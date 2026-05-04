-- 多周期结算、平台抽成配置测试数据存储过程
-- 说明：
-- 1. 依赖已有 merchant / orders / merchant_settlement 数据基础
-- 2. 商户批次通过 merchant.merchant_code 前缀标识
-- 3. 本文件包含多周期结算造数、抽成配置造数、清理过程、校验 SQL

DROP PROCEDURE IF EXISTS sp_mock_multi_cycle_settlement_by_batch;
DROP PROCEDURE IF EXISTS sp_clear_multi_cycle_settlement_by_batch;
DROP PROCEDURE IF EXISTS sp_mock_platform_commission_config;
DROP PROCEDURE IF EXISTS sp_clear_platform_commission_mock;

DELIMITER $$

CREATE PROCEDURE sp_mock_multi_cycle_settlement_by_batch(
    IN p_merchant_batch_no VARCHAR(32),
    IN p_end_cycle VARCHAR(16),
    IN p_cycle_count INT,
    IN p_create_user BIGINT
)
BEGIN
    DECLARE v_done INT DEFAULT 0;
    DECLARE v_merchant_id BIGINT;
    DECLARE v_batch_code VARCHAR(32);
    DECLARE v_now DATETIME;
    DECLARE v_operator_id BIGINT DEFAULT 1;
    DECLARE v_end_month DATE;
    DECLARE v_cycle_index INT DEFAULT 0;
    DECLARE v_cycle_date DATE;
    DECLARE v_cycle_label VARCHAR(16);
    DECLARE v_settlement_status INT;
    DECLARE v_settlement_time DATETIME;
    DECLARE v_order_count INT DEFAULT 0;
    DECLARE v_total_amount DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_platform_commission DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_merchant_amount DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_settlement_amount DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_scale DECIMAL(10,2) DEFAULT 1.00;

    DECLARE cur_merchant CURSOR FOR
        SELECT m.id
        FROM merchant m
        WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(v_batch_code))
              = CAST(v_batch_code AS BINARY)
        ORDER BY m.id;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = 1;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF p_merchant_batch_no IS NULL OR CHAR_LENGTH(TRIM(p_merchant_batch_no)) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '商户批次号不能为空';
    END IF;

    IF p_end_cycle IS NULL OR CHAR_LENGTH(TRIM(p_end_cycle)) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '结束周期不能为空';
    END IF;

    IF p_cycle_count IS NULL OR p_cycle_count <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '周期数必须大于 0';
    END IF;

    SET v_batch_code = UPPER(REPLACE(TRIM(p_merchant_batch_no), '-', ''));
    SET v_operator_id = IFNULL(p_create_user, 1);
    SET v_now = NOW();
    SET v_end_month = STR_TO_DATE(CONCAT(TRIM(p_end_cycle), '-01'), '%Y-%m-%d');

    IF v_end_month IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '结束周期格式错误，应为 yyyy-MM';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM merchant m
        WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(v_batch_code))
              = CAST(v_batch_code AS BINARY)
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '未找到对应商户批次';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM merchant_settlement ms
        JOIN merchant m ON m.id = ms.merchant_id
        WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(v_batch_code))
              = CAST(v_batch_code AS BINARY)
          AND CAST(ms.settlement_cycle AS BINARY) IN (
              SELECT CAST(DATE_FORMAT(DATE_SUB(v_end_month, INTERVAL seq.seq MONTH), '%Y-%m') AS BINARY)
              FROM (
                  SELECT 0 AS seq UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
                  SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL
                  SELECT 10 UNION ALL SELECT 11
              ) seq
              WHERE seq.seq < p_cycle_count
          )
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '目标周期内已存在结算单，请先清理';
    END IF;

    START TRANSACTION;

    OPEN cur_merchant;

    merchant_loop: LOOP
        FETCH cur_merchant INTO v_merchant_id;
        IF v_done = 1 THEN
            LEAVE merchant_loop;
        END IF;

        SET v_cycle_index = 0;
        WHILE v_cycle_index < p_cycle_count DO
            SET v_cycle_date = DATE_SUB(v_end_month, INTERVAL v_cycle_index MONTH);
            SET v_cycle_label = DATE_FORMAT(v_cycle_date, '%Y-%m');

            SELECT COUNT(*),
                   IFNULL(SUM(o.total_amount), 0),
                   IFNULL(SUM(o.platform_commission), 0),
                   IFNULL(SUM(o.merchant_amount), 0)
            INTO v_order_count, v_total_amount, v_platform_commission, v_merchant_amount
            FROM orders o
            WHERE o.merchant_id = v_merchant_id
              AND o.status = 5
              AND o.pay_status = 1;

            IF v_order_count > 0 THEN
                IF v_cycle_index = 0 THEN
                    SET v_scale = 1.00;
                ELSEIF v_cycle_index = 1 THEN
                    SET v_scale = 0.88;
                ELSEIF v_cycle_index = 2 THEN
                    SET v_scale = 0.76;
                ELSE
                    SET v_scale = 0.65;
                END IF;

                SET v_order_count = GREATEST(1, FLOOR(v_order_count / p_cycle_count * (0.8 + RAND() * 0.6)));
                SET v_total_amount = ROUND(v_total_amount / p_cycle_count * v_scale * (0.9 + RAND() * 0.2), 2);
                SET v_platform_commission = ROUND(v_platform_commission / p_cycle_count * v_scale * (0.9 + RAND() * 0.2), 2);
                SET v_merchant_amount = ROUND(v_merchant_amount / p_cycle_count * v_scale * (0.9 + RAND() * 0.2), 2);
                SET v_settlement_amount = v_merchant_amount;

                IF v_cycle_index = 0 THEN
                    SET v_settlement_status = 1;
                    SET v_settlement_time = NULL;
                ELSE
                    IF RAND() < 0.85 THEN
                        SET v_settlement_status = 2;
                        SET v_settlement_time = DATE_ADD(LAST_DAY(v_cycle_date), INTERVAL (1 + FLOOR(RAND() * 5)) DAY);
                    ELSE
                        SET v_settlement_status = 3;
                        SET v_settlement_time = NULL;
                    END IF;
                END IF;

                INSERT INTO merchant_settlement (
                    merchant_id,
                    settlement_cycle,
                    order_count,
                    total_amount,
                    platform_commission,
                    merchant_amount,
                    settlement_amount,
                    status,
                    settlement_time,
                    remark,
                    create_time,
                    update_time
                ) VALUES (
                    v_merchant_id,
                    v_cycle_label,
                    v_order_count,
                    v_total_amount,
                    v_platform_commission,
                    v_merchant_amount,
                    v_settlement_amount,
                    v_settlement_status,
                    v_settlement_time,
                    CONCAT('批量生成-', v_cycle_label),
                    v_now,
                    v_now
                );
            END IF;

            SET v_cycle_index = v_cycle_index + 1;
        END WHILE;
    END LOOP;

    CLOSE cur_merchant;

    COMMIT;
END$$

CREATE PROCEDURE sp_clear_multi_cycle_settlement_by_batch(
    IN p_merchant_batch_no VARCHAR(32),
    IN p_begin_cycle VARCHAR(16),
    IN p_end_cycle VARCHAR(16)
)
BEGIN
    DECLARE v_batch_code VARCHAR(32);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF p_merchant_batch_no IS NULL OR CHAR_LENGTH(TRIM(p_merchant_batch_no)) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '商户批次号不能为空';
    END IF;

    SET v_batch_code = UPPER(REPLACE(TRIM(p_merchant_batch_no), '-', ''));

    START TRANSACTION;

    DELETE ms
    FROM merchant_settlement ms
    JOIN merchant m ON m.id = ms.merchant_id
    WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(v_batch_code))
          = CAST(v_batch_code AS BINARY)
      AND (p_begin_cycle IS NULL OR CHAR_LENGTH(TRIM(p_begin_cycle)) = 0 OR CAST(ms.settlement_cycle AS BINARY) >= CAST(TRIM(p_begin_cycle) AS BINARY))
      AND (p_end_cycle IS NULL OR CHAR_LENGTH(TRIM(p_end_cycle)) = 0 OR CAST(ms.settlement_cycle AS BINARY) <= CAST(TRIM(p_end_cycle) AS BINARY));

    COMMIT;
END$$

CREATE PROCEDURE sp_mock_platform_commission_config(
    IN p_merchant_batch_no VARCHAR(32),
    IN p_create_user BIGINT
)
BEGIN
    DECLARE v_batch_code VARCHAR(32);
    DECLARE v_now DATETIME;
    DECLARE v_operator_id BIGINT DEFAULT 1;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF p_merchant_batch_no IS NULL OR CHAR_LENGTH(TRIM(p_merchant_batch_no)) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '商户批次号不能为空';
    END IF;

    SET v_batch_code = UPPER(REPLACE(TRIM(p_merchant_batch_no), '-', ''));
    SET v_now = NOW();
    SET v_operator_id = IFNULL(p_create_user, 1);

    IF EXISTS (
        SELECT 1 FROM platform_commission pc
        WHERE (pc.config_type = 1)
           OR (pc.config_type = 2 AND pc.merchant_id IN (
                SELECT m.id FROM merchant m
                WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(v_batch_code))
                      = CAST(v_batch_code AS BINARY)
           ))
           OR (pc.config_type = 3 AND pc.category_id IN (1,2,3,4))
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '检测到已有抽成配置，请先清理后再生成';
    END IF;

    START TRANSACTION;

    INSERT INTO platform_commission (
        config_type,
        merchant_id,
        category_id,
        commission_rate,
        effective_time,
        expire_time,
        status,
        create_time,
        update_time,
        create_user,
        update_user
    ) VALUES (
        1,
        NULL,
        NULL,
        18.00,
        v_now,
        NULL,
        1,
        v_now,
        v_now,
        v_operator_id,
        v_operator_id
    );

    INSERT INTO platform_commission (
        config_type,
        merchant_id,
        category_id,
        commission_rate,
        effective_time,
        expire_time,
        status,
        create_time,
        update_time,
        create_user,
        update_user
    ) VALUES
    (3, NULL, 1, 16.00, v_now, NULL, 1, v_now, v_now, v_operator_id, v_operator_id),
    (3, NULL, 2, 19.00, v_now, NULL, 1, v_now, v_now, v_operator_id, v_operator_id),
    (3, NULL, 3, 12.00, v_now, NULL, 1, v_now, v_now, v_operator_id, v_operator_id),
    (3, NULL, 4, 10.00, v_now, NULL, 1, v_now, v_now, v_operator_id, v_operator_id);

    INSERT INTO platform_commission (
        config_type,
        merchant_id,
        category_id,
        commission_rate,
        effective_time,
        expire_time,
        status,
        create_time,
        update_time,
        create_user,
        update_user
    )
    SELECT
        2,
        t.id,
        NULL,
        CASE MOD(t.id, 5)
            WHEN 0 THEN 11.50
            WHEN 1 THEN 13.00
            WHEN 2 THEN 14.50
            WHEN 3 THEN 15.50
            ELSE 17.00
        END,
        v_now,
        NULL,
        1,
        v_now,
        v_now,
        v_operator_id,
        v_operator_id
    FROM (
        SELECT m.id
        FROM merchant m
        WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(v_batch_code))
              = CAST(v_batch_code AS BINARY)
        ORDER BY m.id
        LIMIT 12
    ) t;

    COMMIT;
END$$

CREATE PROCEDURE sp_clear_platform_commission_mock(
    IN p_merchant_batch_no VARCHAR(32)
)
BEGIN
    DECLARE v_batch_code VARCHAR(32);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF p_merchant_batch_no IS NULL OR CHAR_LENGTH(TRIM(p_merchant_batch_no)) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '商户批次号不能为空';
    END IF;

    SET v_batch_code = UPPER(REPLACE(TRIM(p_merchant_batch_no), '-', ''));

    START TRANSACTION;

    DELETE FROM platform_commission
    WHERE config_type = 1
       OR (config_type = 3 AND category_id IN (1,2,3,4))
       OR (config_type = 2 AND merchant_id IN (
            SELECT id FROM (
                SELECT m.id
                FROM merchant m
                WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(v_batch_code))
                      = CAST(v_batch_code AS BINARY)
            ) x
       ));

    COMMIT;
END$$

DELIMITER ;

-- =========================
-- 示例执行顺序
-- =========================
-- 1. 为近 4 个周期生成结算单（结束周期 2026-04）
-- CALL sp_mock_multi_cycle_settlement_by_batch('T250425A', '2026-04', 4, 1);
--
-- 2. 生成平台抽成配置
-- CALL sp_mock_platform_commission_config('T250425A', 1);
--
-- 3. 清理 2026-01 ~ 2026-04 的结算单
-- CALL sp_clear_multi_cycle_settlement_by_batch('T250425A', '2026-01', '2026-04');
--
-- 4. 清理抽成配置
-- CALL sp_clear_platform_commission_mock('T250425A');
--
-- =========================
-- 快速校验 SQL
-- =========================
-- SET @merchant_batch = 'T250425A';
--
-- SELECT settlement_cycle, status, COUNT(*) AS cnt, SUM(settlement_amount) AS total_amount
-- FROM merchant_settlement ms
-- JOIN merchant m ON m.id = ms.merchant_id
-- WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(UPPER(REPLACE(@merchant_batch, '-', ''))))
--       = CAST(UPPER(REPLACE(@merchant_batch, '-', '')) AS BINARY)
--   AND CAST(ms.settlement_cycle AS BINARY) BETWEEN CAST('2026-01' AS BINARY) AND CAST('2026-04' AS BINARY)
-- GROUP BY settlement_cycle, status
-- ORDER BY settlement_cycle, status;
--
-- SELECT config_type, COUNT(*) AS cnt, AVG(commission_rate) AS avg_rate
-- FROM platform_commission
-- GROUP BY config_type
-- ORDER BY config_type;
--
-- SELECT pc.config_type, pc.merchant_id, pc.category_id, pc.commission_rate, pc.status
-- FROM platform_commission pc
-- ORDER BY pc.config_type, pc.merchant_id, pc.category_id;
