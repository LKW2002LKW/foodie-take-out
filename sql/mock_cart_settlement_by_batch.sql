-- 购物车、结算测试数据存储过程
-- 说明：
-- 1. 依赖已有 merchant / dish / setmeal / user / orders 等测试数据
-- 2. 用户批次通过 user.openid 前缀 UBxxxx 标识，商户批次通过 merchant.merchant_code 前缀标识
-- 3. 本文件包含购物车造数、结算造数、清理过程、校验 SQL

DROP PROCEDURE IF EXISTS sp_mock_cart_by_batch;
DROP PROCEDURE IF EXISTS sp_clear_cart_by_batch;
DROP PROCEDURE IF EXISTS sp_mock_settlement_by_batch;
DROP PROCEDURE IF EXISTS sp_clear_settlement_by_batch;

DELIMITER $$

CREATE PROCEDURE sp_mock_cart_by_batch(
    IN p_merchant_batch_no VARCHAR(32),
    IN p_user_batch_no VARCHAR(32),
    IN p_cart_user_count INT,
    IN p_max_items_per_merchant INT
)
BEGIN
    DECLARE v_done INT DEFAULT 0;
    DECLARE v_user_id BIGINT;
    DECLARE v_batch_user_code VARCHAR(32);
    DECLARE v_batch_merchant_code VARCHAR(32);
    DECLARE v_now DATETIME;
    DECLARE v_selected_users INT DEFAULT 0;
    DECLARE v_merchant_loop INT DEFAULT 1;
    DECLARE v_merchant_pick_count INT DEFAULT 0;
    DECLARE v_item_loop INT DEFAULT 1;
    DECLARE v_item_count INT DEFAULT 0;
    DECLARE v_merchant_id BIGINT;
    DECLARE v_has_dish INT DEFAULT 0;
    DECLARE v_has_setmeal INT DEFAULT 0;
    DECLARE v_dish_id BIGINT;
    DECLARE v_setmeal_id BIGINT;
    DECLARE v_name VARCHAR(128);
    DECLARE v_image VARCHAR(255);
    DECLARE v_flavor VARCHAR(255);
    DECLARE v_amount DECIMAL(10,2);
    DECLARE v_number INT DEFAULT 1;

    DECLARE cur_user CURSOR FOR
        SELECT u.id
        FROM `user` u
        WHERE LEFT(CAST(u.openid AS BINARY), CHAR_LENGTH(CONCAT('UB', v_batch_user_code)))
              = CAST(CONCAT('UB', v_batch_user_code) AS BINARY)
        ORDER BY RAND();

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = 1;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF p_merchant_batch_no IS NULL OR CHAR_LENGTH(TRIM(p_merchant_batch_no)) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '商户批次号不能为空';
    END IF;

    IF p_user_batch_no IS NULL OR CHAR_LENGTH(TRIM(p_user_batch_no)) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '用户批次号不能为空';
    END IF;

    IF p_cart_user_count IS NULL OR p_cart_user_count <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '购物车用户数必须大于 0';
    END IF;

    IF p_max_items_per_merchant IS NULL OR p_max_items_per_merchant <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '每商户购物车商品上限必须大于 0';
    END IF;

    SET v_batch_user_code = UPPER(REPLACE(TRIM(p_user_batch_no), '-', ''));
    SET v_batch_merchant_code = UPPER(REPLACE(TRIM(p_merchant_batch_no), '-', ''));
    SET v_now = NOW();

    IF NOT EXISTS (
        SELECT 1 FROM `user` u
        WHERE LEFT(CAST(u.openid AS BINARY), CHAR_LENGTH(CONCAT('UB', v_batch_user_code)))
              = CAST(CONCAT('UB', v_batch_user_code) AS BINARY)
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '未找到对应用户批次';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM merchant m
        WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(v_batch_merchant_code))
              = CAST(v_batch_merchant_code AS BINARY)
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '未找到对应商户批次';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM shopping_cart sc
        JOIN `user` u ON u.id = sc.user_id
        WHERE LEFT(CAST(u.openid AS BINARY), CHAR_LENGTH(CONCAT('UB', v_batch_user_code)))
              = CAST(CONCAT('UB', v_batch_user_code) AS BINARY)
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '当前用户批次已存在购物车数据，请先清理';
    END IF;

    START TRANSACTION;

    OPEN cur_user;

    user_loop: LOOP
        FETCH cur_user INTO v_user_id;
        IF v_done = 1 OR v_selected_users >= p_cart_user_count THEN
            LEAVE user_loop;
        END IF;

        SET v_merchant_pick_count = 1 + FLOOR(RAND() * 3);
        SET v_merchant_loop = 1;

        WHILE v_merchant_loop <= v_merchant_pick_count DO
            SELECT MAX(t.id)
            INTO v_merchant_id
            FROM (
                SELECT m.id
                FROM merchant m
                WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(v_batch_merchant_code))
                      = CAST(v_batch_merchant_code AS BINARY)
                  AND m.audit_status = 1
                  AND m.status IN (1, 2)
                  AND m.id NOT IN (
                      SELECT sc.merchant_id
                      FROM shopping_cart sc
                      WHERE sc.user_id = v_user_id
                  )
                ORDER BY RAND()
                LIMIT 1
            ) t;

            IF v_merchant_id IS NULL THEN
                SET v_merchant_loop = v_merchant_pick_count + 1;
            ELSE
                SELECT COUNT(*) INTO v_has_dish
                FROM dish d
                WHERE d.merchant_id = v_merchant_id AND d.status = 1;

                SELECT COUNT(*) INTO v_has_setmeal
                FROM setmeal s
                WHERE s.merchant_id = v_merchant_id AND s.status = 1;

                SET v_item_count = 1 + FLOOR(RAND() * p_max_items_per_merchant);
                SET v_item_loop = 1;

                WHILE v_item_loop <= v_item_count DO
                    SET v_dish_id = NULL;
                    SET v_setmeal_id = NULL;
                    SET v_flavor = NULL;
                    SET v_number = 1 + FLOOR(RAND() * 3);

                    IF v_has_setmeal > 0 AND (v_has_dish = 0 OR RAND() < 0.30) THEN
                        SELECT
                            MAX(t.id),
                            MAX(t.name),
                            MAX(t.image),
                            MAX(t.price)
                        INTO v_setmeal_id, v_name, v_image, v_amount
                        FROM (
                            SELECT s.id, s.name, s.image, s.price
                            FROM setmeal s
                            WHERE s.merchant_id = v_merchant_id
                              AND s.status = 1
                              AND s.id NOT IN (
                                  SELECT IFNULL(sc.setmeal_id, -1)
                                  FROM shopping_cart sc
                                  WHERE sc.user_id = v_user_id
                                    AND sc.merchant_id = v_merchant_id
                              )
                            ORDER BY RAND()
                            LIMIT 1
                        ) t;
                    ELSE
                        SELECT
                            MAX(t.id),
                            MAX(t.name),
                            MAX(t.image),
                            MAX(t.price)
                        INTO v_dish_id, v_name, v_image, v_amount
                        FROM (
                            SELECT d.id, d.name, d.image, d.price
                            FROM dish d
                            WHERE d.merchant_id = v_merchant_id
                              AND d.status = 1
                              AND d.id NOT IN (
                                  SELECT IFNULL(sc.dish_id, -1)
                                  FROM shopping_cart sc
                                  WHERE sc.user_id = v_user_id
                                    AND sc.merchant_id = v_merchant_id
                              )
                            ORDER BY RAND()
                            LIMIT 1
                        ) t;

                        IF v_dish_id IS NOT NULL THEN
                            SELECT MAX(t.value)
                            INTO v_flavor
                            FROM (
                                SELECT df.value
                                FROM dish_flavor df
                                WHERE df.dish_id = v_dish_id
                                ORDER BY RAND()
                                LIMIT 1
                            ) t;
                        END IF;
                    END IF;

                    IF v_dish_id IS NOT NULL OR v_setmeal_id IS NOT NULL THEN
                        INSERT INTO shopping_cart (
                            user_id,
                            merchant_id,
                            dish_id,
                            setmeal_id,
                            name,
                            image,
                            dish_flavor,
                            number,
                            amount,
                            create_time
                        ) VALUES (
                            v_user_id,
                            v_merchant_id,
                            v_dish_id,
                            v_setmeal_id,
                            v_name,
                            v_image,
                            v_flavor,
                            v_number,
                            v_amount,
                            DATE_SUB(v_now, INTERVAL FLOOR(RAND() * 1440) MINUTE)
                        );
                    END IF;

                    SET v_item_loop = v_item_loop + 1;
                END WHILE;

                SET v_merchant_loop = v_merchant_loop + 1;
            END IF;
        END WHILE;

        SET v_selected_users = v_selected_users + 1;
    END LOOP;

    CLOSE cur_user;

    COMMIT;
END$$

CREATE PROCEDURE sp_clear_cart_by_batch(
    IN p_user_batch_no VARCHAR(32)
)
BEGIN
    DECLARE v_batch_user_code VARCHAR(32);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF p_user_batch_no IS NULL OR CHAR_LENGTH(TRIM(p_user_batch_no)) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '用户批次号不能为空';
    END IF;

    SET v_batch_user_code = UPPER(REPLACE(TRIM(p_user_batch_no), '-', ''));

    START TRANSACTION;

    DELETE sc
    FROM shopping_cart sc
    JOIN `user` u ON u.id = sc.user_id
    WHERE LEFT(CAST(u.openid AS BINARY), CHAR_LENGTH(CONCAT('UB', v_batch_user_code)))
          = CAST(CONCAT('UB', v_batch_user_code) AS BINARY);

    COMMIT;
END$$

CREATE PROCEDURE sp_mock_settlement_by_batch(
    IN p_merchant_batch_no VARCHAR(32),
    IN p_settlement_cycle VARCHAR(16),
    IN p_default_status INT
)
BEGIN
    DECLARE v_done INT DEFAULT 0;
    DECLARE v_merchant_id BIGINT;
    DECLARE v_batch_merchant_code VARCHAR(32);
    DECLARE v_cycle VARCHAR(16);
    DECLARE v_begin_time DATETIME;
    DECLARE v_end_time DATETIME;
    DECLARE v_order_count INT DEFAULT 0;
    DECLARE v_total_amount DECIMAL(10,2);
    DECLARE v_platform_commission DECIMAL(10,2);
    DECLARE v_merchant_amount DECIMAL(10,2);
    DECLARE v_settlement_amount DECIMAL(10,2);
    DECLARE v_status INT DEFAULT 1;
    DECLARE v_settlement_time DATETIME;
    DECLARE v_remark VARCHAR(255);
    DECLARE v_now DATETIME;

    DECLARE cur_merchant CURSOR FOR
        SELECT m.id
        FROM merchant m
        WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(v_batch_merchant_code))
              = CAST(v_batch_merchant_code AS BINARY)
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

    IF p_settlement_cycle IS NULL OR CHAR_LENGTH(TRIM(p_settlement_cycle)) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '结算周期不能为空';
    END IF;

    SET v_batch_merchant_code = UPPER(REPLACE(TRIM(p_merchant_batch_no), '-', ''));
    SET v_cycle = TRIM(p_settlement_cycle);
    SET v_now = NOW();
    SET v_status = IFNULL(p_default_status, 1);
    SET v_begin_time = STR_TO_DATE(CONCAT(v_cycle, '-01 00:00:00'), '%Y-%m-%d %H:%i:%s');
    SET v_end_time = DATE_SUB(DATE_ADD(LAST_DAY(v_begin_time), INTERVAL 1 DAY), INTERVAL 1 SECOND);

    IF NOT EXISTS (
        SELECT 1 FROM merchant m
        WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(v_batch_merchant_code))
              = CAST(v_batch_merchant_code AS BINARY)
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '未找到对应商户批次';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM merchant_settlement ms
        JOIN merchant m ON m.id = ms.merchant_id
        WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(v_batch_merchant_code))
              = CAST(v_batch_merchant_code AS BINARY)
          AND CAST(ms.settlement_cycle AS BINARY) = CAST(v_cycle AS BINARY)
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '当前周期结算单已存在，请先清理';
    END IF;

    START TRANSACTION;

    OPEN cur_merchant;

    merchant_loop: LOOP
        FETCH cur_merchant INTO v_merchant_id;
        IF v_done = 1 THEN
            LEAVE merchant_loop;
        END IF;

        SELECT COUNT(*),
               IFNULL(SUM(o.total_amount), 0),
               IFNULL(SUM(o.platform_commission), 0),
               IFNULL(SUM(o.merchant_amount), 0)
        INTO v_order_count, v_total_amount, v_platform_commission, v_merchant_amount
        FROM orders o
        WHERE o.merchant_id = v_merchant_id
          AND o.status = 5
          AND o.pay_status = 1
          AND o.order_time BETWEEN v_begin_time AND v_end_time;

        IF v_order_count > 0 THEN
            SET v_settlement_amount = v_merchant_amount;
            SET v_settlement_time = NULL;
            SET v_remark = NULL;

            IF v_status = 2 THEN
                SET v_settlement_time = DATE_ADD(v_end_time, INTERVAL (1 + FLOOR(RAND() * 5)) DAY);
                SET v_remark = '批量生成-已结算';
            ELSEIF v_status = 3 THEN
                SET v_settlement_time = NULL;
                SET v_remark = '批量生成-已取消';
            ELSE
                SET v_remark = '批量生成-待结算';
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
                v_cycle,
                v_order_count,
                ROUND(v_total_amount, 2),
                ROUND(v_platform_commission, 2),
                ROUND(v_merchant_amount, 2),
                ROUND(v_settlement_amount, 2),
                v_status,
                v_settlement_time,
                v_remark,
                v_now,
                v_now
            );
        END IF;
    END LOOP;

    CLOSE cur_merchant;

    COMMIT;
END$$

CREATE PROCEDURE sp_clear_settlement_by_batch(
    IN p_merchant_batch_no VARCHAR(32),
    IN p_settlement_cycle VARCHAR(16)
)
BEGIN
    DECLARE v_batch_merchant_code VARCHAR(32);
    DECLARE v_cycle VARCHAR(16);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF p_merchant_batch_no IS NULL OR CHAR_LENGTH(TRIM(p_merchant_batch_no)) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '商户批次号不能为空';
    END IF;

    SET v_batch_merchant_code = UPPER(REPLACE(TRIM(p_merchant_batch_no), '-', ''));
    SET v_cycle = TRIM(IFNULL(p_settlement_cycle, ''));

    START TRANSACTION;

    DELETE ms
    FROM merchant_settlement ms
    JOIN merchant m ON m.id = ms.merchant_id
    WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(v_batch_merchant_code))
          = CAST(v_batch_merchant_code AS BINARY)
      AND (CHAR_LENGTH(v_cycle) = 0 OR CAST(ms.settlement_cycle AS BINARY) = CAST(v_cycle AS BINARY));

    COMMIT;
END$$

DELIMITER ;

-- =========================
-- 示例执行顺序
-- =========================
-- 1. 生成购物车数据
-- CALL sp_mock_cart_by_batch('T250425A', 'U250425A', 20, 4);
--
-- 2. 生成 2026-04 结算单，状态 1=待结算，2=已结算，3=已取消
-- CALL sp_mock_settlement_by_batch('T250425A', '2026-04', 1);
--
-- 3. 如需清理购物车
-- CALL sp_clear_cart_by_batch('U250425A');
--
-- 4. 如需清理结算单
-- CALL sp_clear_settlement_by_batch('T250425A', '2026-04');
--
-- =========================
-- 快速校验 SQL
-- =========================
-- SET @merchant_batch = 'T250425A';
-- SET @user_batch = 'U250425A';
--
-- SELECT COUNT(*) AS cart_row_count
-- FROM shopping_cart sc
-- JOIN `user` u ON u.id = sc.user_id
-- WHERE LEFT(CAST(u.openid AS BINARY), CHAR_LENGTH(CONCAT('UB', UPPER(REPLACE(@user_batch, '-', '')))))
--       = CAST(CONCAT('UB', UPPER(REPLACE(@user_batch, '-', ''))) AS BINARY);
--
-- SELECT sc.user_id, COUNT(DISTINCT sc.merchant_id) AS merchant_count, COUNT(*) AS cart_item_count
-- FROM shopping_cart sc
-- JOIN `user` u ON u.id = sc.user_id
-- WHERE LEFT(CAST(u.openid AS BINARY), CHAR_LENGTH(CONCAT('UB', UPPER(REPLACE(@user_batch, '-', '')))))
--       = CAST(CONCAT('UB', UPPER(REPLACE(@user_batch, '-', ''))) AS BINARY)
-- GROUP BY sc.user_id
-- ORDER BY cart_item_count DESC, merchant_count DESC;
--
-- SELECT COUNT(*) AS settlement_count
-- FROM merchant_settlement ms
-- JOIN merchant m ON m.id = ms.merchant_id
-- WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(UPPER(REPLACE(@merchant_batch, '-', ''))))
--       = CAST(UPPER(REPLACE(@merchant_batch, '-', '')) AS BINARY)
--   AND CAST(ms.settlement_cycle AS BINARY) = CAST('2026-04' AS BINARY);
--
-- SELECT ms.status, COUNT(*) AS cnt, SUM(ms.settlement_amount) AS total_settlement_amount
-- FROM merchant_settlement ms
-- JOIN merchant m ON m.id = ms.merchant_id
-- WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(UPPER(REPLACE(@merchant_batch, '-', ''))))
--       = CAST(UPPER(REPLACE(@merchant_batch, '-', '')) AS BINARY)
--   AND CAST(ms.settlement_cycle AS BINARY) = CAST('2026-04' AS BINARY)
-- GROUP BY ms.status
-- ORDER BY ms.status;
