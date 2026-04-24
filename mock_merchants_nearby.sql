-- 100 个商户测试数据存储过程
-- 目标：
-- 1. 以指定经纬度为中心，在指定半径内生成商户
-- 2. 同时插入 merchant / merchant_admin
-- 3. 存储过程内部使用事务，发生 SQL 异常时自动回滚
-- 4. 提供按批次清理 SQL 和距离校验 SQL
--
-- 使用前提：
-- 1. merchant / merchant_admin 两张表均为 InnoDB
-- 2. 本脚本不包含 DDL 事务保护；仅存储过程内部的批量 INSERT 可回滚
-- 3. 管理员初始明文密码固定为：123456
-- 4. 过程里会显式写入 create_time / update_time / create_user / update_user

DROP PROCEDURE IF EXISTS sp_mock_merchants_nearby;

DELIMITER $$

CREATE PROCEDURE sp_mock_merchants_nearby(
    IN p_center_lng DECIMAL(10,6),
    IN p_center_lat DECIMAL(10,6),
    IN p_radius_km DECIMAL(10,2),
    IN p_total INT,
    IN p_batch_no VARCHAR(32),
    IN p_create_user BIGINT
)
BEGIN
    DECLARE v_i INT DEFAULT 1;
    DECLARE v_category_id INT;
    DECLARE v_status INT;
    DECLARE v_theta DOUBLE;
    DECLARE v_distance_km DOUBLE;
    DECLARE v_delta_lat DOUBLE;
    DECLARE v_delta_lng DOUBLE;
    DECLARE v_lng DECIMAL(10,6);
    DECLARE v_lat DECIMAL(10,6);
    DECLARE v_merchant_id BIGINT;
    DECLARE v_operator_id BIGINT DEFAULT 1;
    DECLARE v_batch_code VARCHAR(32);
    DECLARE v_admin_prefix VARCHAR(40);
    DECLARE v_merchant_name VARCHAR(64);
    DECLARE v_merchant_code VARCHAR(32);
    DECLARE v_business_license VARCHAR(18);
    DECLARE v_legal_person VARCHAR(32);
    DECLARE v_contact_name VARCHAR(32);
    DECLARE v_contact_phone VARCHAR(11);
    DECLARE v_description VARCHAR(255);
    DECLARE v_business_hours VARCHAR(32);
    DECLARE v_min_delivery_amount DECIMAL(10,2);
    DECLARE v_delivery_fee DECIMAL(10,2);
    DECLARE v_commission_rate DECIMAL(10,2);
    DECLARE v_username VARCHAR(64);
    DECLARE v_admin_name VARCHAR(32);
    DECLARE v_admin_phone VARCHAR(11);
    DECLARE v_id_card VARCHAR(18);
    DECLARE v_logo VARCHAR(255);
    DECLARE v_now DATETIME;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF p_center_lng IS NULL OR p_center_lat IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '中心经纬度不能为空';
    END IF;

    IF p_radius_km IS NULL OR p_radius_km <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '半径必须大于 0';
    END IF;

    IF p_total IS NULL OR p_total <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '生成数量必须大于 0';
    END IF;

    IF p_batch_no IS NULL OR TRIM(p_batch_no) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '批次号不能为空';
    END IF;

    SET v_operator_id = IFNULL(p_create_user, 1);
    SET v_now = NOW();
    SET v_batch_code = UPPER(REPLACE(TRIM(p_batch_no), '-', ''));
    SET v_admin_prefix = CONCAT('tm', LOWER(REPLACE(TRIM(p_batch_no), '-', '')));
    SET v_logo = 'https://dummyimage.com/200x200/f5c200/ffffff&text=Foodie';
    SET v_commission_rate = 18.00;

    IF EXISTS (
        SELECT 1
        FROM merchant
        WHERE LEFT(CAST(merchant_code AS BINARY), CHAR_LENGTH(v_batch_code))
              = CAST(v_batch_code AS BINARY)
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '当前批次 merchant 数据已存在，请更换批次号或先清理';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM merchant_admin
        WHERE LEFT(CAST(username AS BINARY), CHAR_LENGTH(v_admin_prefix))
              = CAST(v_admin_prefix AS BINARY)
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '当前批次 merchant_admin 数据已存在，请更换批次号或先清理';
    END IF;

    START TRANSACTION;

    WHILE v_i <= p_total DO
        SET v_category_id = MOD(v_i - 1, 4) + 1;
        SET v_status = IF(RAND() < 0.75, 1, 2);
        SET v_theta = RAND() * 2 * PI();
        SET v_distance_km = p_radius_km * SQRT(RAND());

        -- 近似换算：1 纬度约等于 111.32km，1 经度约等于 111.32 * cos(latitude) km
        SET v_delta_lat = (v_distance_km / 111.32) * SIN(v_theta);
        SET v_delta_lng = (v_distance_km / (111.32 * COS(RADIANS(p_center_lat)))) * COS(v_theta);
        SET v_lng = ROUND(p_center_lng + v_delta_lng, 6);
        SET v_lat = ROUND(p_center_lat + v_delta_lat, 6);

        SET v_merchant_code = CONCAT(v_batch_code, LPAD(v_i, 4, '0'));
        SET v_business_license = CONCAT('MOCK', UPPER(LEFT(MD5(CONCAT(v_batch_code, '-', v_i)), 14)));
        SET v_contact_phone = CONCAT('13', LPAD(MOD(CRC32(CONCAT(v_batch_code, '-phone-', v_i)), 1000000000), 9, '0'));
        SET v_id_card = CONCAT('320', LPAD(MOD(CRC32(CONCAT(v_batch_code, '-idcard-', v_i)), 1000000000000000), 15, '0'));
        SET v_username = CONCAT(v_admin_prefix, LPAD(v_i, 3, '0'));

        CASE v_category_id
            WHEN 1 THEN
                SET v_merchant_name = CONCAT('美食测试店', LPAD(v_i, 3, '0'));
                SET v_legal_person = CONCAT('美食法人', LPAD(v_i, 3, '0'));
                SET v_contact_name = CONCAT('美食店长', LPAD(v_i, 3, '0'));
                SET v_admin_name = CONCAT('美食管理员', LPAD(v_i, 3, '0'));
                SET v_description = '现炒快餐、盖浇饭、面食小炒';
                SET v_business_hours = '09:00-21:00';
                SET v_min_delivery_amount = ROUND(18 + RAND() * 12, 2);
                SET v_delivery_fee = ROUND(3 + RAND() * 4, 2);
            WHEN 2 THEN
                SET v_merchant_name = CONCAT('甜饮测试店', LPAD(v_i, 3, '0'));
                SET v_legal_person = CONCAT('甜饮法人', LPAD(v_i, 3, '0'));
                SET v_contact_name = CONCAT('甜饮店长', LPAD(v_i, 3, '0'));
                SET v_admin_name = CONCAT('甜饮管理员', LPAD(v_i, 3, '0'));
                SET v_description = '奶茶咖啡、甜品蛋糕、下午茶';
                SET v_business_hours = '10:00-22:00';
                SET v_min_delivery_amount = ROUND(15 + RAND() * 10, 2);
                SET v_delivery_fee = ROUND(2 + RAND() * 4, 2);
            WHEN 3 THEN
                SET v_merchant_name = CONCAT('便利测试店', LPAD(v_i, 3, '0'));
                SET v_legal_person = CONCAT('便利法人', LPAD(v_i, 3, '0'));
                SET v_contact_name = CONCAT('便利店长', LPAD(v_i, 3, '0'));
                SET v_admin_name = CONCAT('便利管理员', LPAD(v_i, 3, '0'));
                SET v_description = '日用百货、零食饮料、应急用品';
                SET v_business_hours = '08:00-23:00';
                SET v_min_delivery_amount = ROUND(20 + RAND() * 15, 2);
                SET v_delivery_fee = ROUND(3 + RAND() * 5, 2);
            ELSE
                SET v_merchant_name = CONCAT('果蔬测试店', LPAD(v_i, 3, '0'));
                SET v_legal_person = CONCAT('果蔬法人', LPAD(v_i, 3, '0'));
                SET v_contact_name = CONCAT('果蔬店长', LPAD(v_i, 3, '0'));
                SET v_admin_name = CONCAT('果蔬管理员', LPAD(v_i, 3, '0'));
                SET v_description = '时令水果、新鲜蔬菜、轻食食材';
                SET v_business_hours = '07:30-21:30';
                SET v_min_delivery_amount = ROUND(16 + RAND() * 12, 2);
                SET v_delivery_fee = ROUND(2 + RAND() * 4, 2);
        END CASE;

        SET v_admin_phone = v_contact_phone;

        INSERT INTO merchant (
            merchant_name,
            merchant_code,
            business_license,
            legal_person,
            contact_name,
            contact_phone,
            province_code,
            province_name,
            city_code,
            city_name,
            district_code,
            district_name,
            address,
            longitude,
            latitude,
            logo,
            description,
            biz_category_id,
            business_hours,
            min_delivery_amount,
            delivery_fee,
            commission_rate,
            status,
            audit_status,
            audit_reason,
            create_time,
            update_time,
            create_user,
            update_user
        ) VALUES (
            v_merchant_name,
            v_merchant_code,
            v_business_license,
            v_legal_person,
            v_contact_name,
            v_contact_phone,
            '320000',
            '江苏省',
            '321000',
            '扬州市',
            '321003',
            '邗江区',
            CONCAT('测试地址-', v_batch_code, '-', LPAD(v_i, 3, '0'), '号'),
            v_lng,
            v_lat,
            v_logo,
            v_description,
            v_category_id,
            v_business_hours,
            v_min_delivery_amount,
            v_delivery_fee,
            v_commission_rate,
            v_status,
            1,
            '批量测试数据-自动通过',
            v_now,
            v_now,
            v_operator_id,
            v_operator_id
        );

        SET v_merchant_id = LAST_INSERT_ID();

        INSERT INTO merchant_admin (
            merchant_id,
            username,
            password,
            name,
            phone,
            id_card,
            role,
            status,
            create_time,
            update_time,
            create_user,
            update_user
        ) VALUES (
            v_merchant_id,
            v_username,
            'e10adc3949ba59abbe56e057f20f883e',
            v_admin_name,
            v_admin_phone,
            v_id_card,
            1,
            1,
            v_now,
            v_now,
            v_operator_id,
            v_operator_id
        );

        SET v_i = v_i + 1;
    END WHILE;

    COMMIT;
END$$

DELIMITER ;

-- =========================
-- 示例调用
-- =========================
-- 建议每次换一个新的批次号，便于清理
-- 管理员初始明文密码：123456
--
-- CALL sp_mock_merchants_nearby(
--     119.943466,
--     32.464328,
--     5.00,
--     100,
--     'T250425A',
--     1
-- );

-- =========================
-- 查询本批次生成结果
-- =========================
-- SET @batch_no = 'T250425A';
--
-- 1. 商户总数
-- SELECT COUNT(*) AS merchant_count
-- FROM merchant
-- WHERE LEFT(CAST(merchant_code AS BINARY), CHAR_LENGTH(UPPER(REPLACE(@batch_no, '-', ''))))
--       = CAST(UPPER(REPLACE(@batch_no, '-', '')) AS BINARY);
--
-- 2. 四类分布
-- SELECT biz_category_id, COUNT(*) AS cnt
-- FROM merchant
-- WHERE LEFT(CAST(merchant_code AS BINARY), CHAR_LENGTH(UPPER(REPLACE(@batch_no, '-', ''))))
--       = CAST(UPPER(REPLACE(@batch_no, '-', '')) AS BINARY)
-- GROUP BY biz_category_id
-- ORDER BY biz_category_id;
--
-- 3. 距离校验：最大距离应 <= 5km
-- SELECT
--     ROUND(MIN(
--         6371 * 2 * ASIN(
--             SQRT(
--                 POW(SIN((32.464328 - latitude) * PI() / 180 / 2), 2) +
--                 COS(32.464328 * PI() / 180) *
--                 COS(latitude * PI() / 180) *
--                 POW(SIN((119.943466 - longitude) * PI() / 180 / 2), 2)
--             )
--         )
--     ), 3) AS min_distance_km,
--     ROUND(MAX(
--         6371 * 2 * ASIN(
--             SQRT(
--                 POW(SIN((32.464328 - latitude) * PI() / 180 / 2), 2) +
--                 COS(32.464328 * PI() / 180) *
--                 COS(latitude * PI() / 180) *
--                 POW(SIN((119.943466 - longitude) * PI() / 180 / 2), 2)
--             )
--         )
--     ), 3) AS max_distance_km,
--     ROUND(AVG(
--         6371 * 2 * ASIN(
--             SQRT(
--                 POW(SIN((32.464328 - latitude) * PI() / 180 / 2), 2) +
--                 COS(32.464328 * PI() / 180) *
--                 COS(latitude * PI() / 180) *
--                 POW(SIN((119.943466 - longitude) * PI() / 180 / 2), 2)
--             )
--         )
--     ), 3) AS avg_distance_km
-- FROM merchant
-- WHERE LEFT(CAST(merchant_code AS BINARY), CHAR_LENGTH(UPPER(REPLACE(@batch_no, '-', ''))))
--       = CAST(UPPER(REPLACE(@batch_no, '-', '')) AS BINARY);
--
-- 4. 查看登录账号
-- SELECT
--     m.id AS merchant_id,
--     m.merchant_name,
--     ma.username,
--     '123456' AS plain_password,
--     ma.status AS admin_status,
--     m.status AS merchant_status,
--     m.audit_status
-- FROM merchant m
-- JOIN merchant_admin ma ON ma.merchant_id = m.id
-- WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(UPPER(REPLACE(@batch_no, '-', ''))))
--       = CAST(UPPER(REPLACE(@batch_no, '-', '')) AS BINARY)
-- ORDER BY m.id;

-- =========================
-- 按批次清理测试数据
-- =========================
-- SET @batch_no = 'T250425A';
-- START TRANSACTION;
--
-- DELETE ma
-- FROM merchant_admin ma
-- JOIN merchant m ON m.id = ma.merchant_id
-- WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(UPPER(REPLACE(@batch_no, '-', ''))))
--       = CAST(UPPER(REPLACE(@batch_no, '-', '')) AS BINARY);
--
-- DELETE FROM merchant
-- WHERE LEFT(CAST(merchant_code AS BINARY), CHAR_LENGTH(UPPER(REPLACE(@batch_no, '-', ''))))
--       = CAST(UPPER(REPLACE(@batch_no, '-', '')) AS BINARY);
--
-- COMMIT;
