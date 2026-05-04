-- 用户、地址、订单、评价测试数据存储过程
-- 说明：
-- 1. 依赖已有 merchant / category / dish / setmeal 等测试数据
-- 2. 用户通过 openid 前缀标识批次，商户通过 merchant_code 前缀标识批次
-- 3. 所有过程内部使用事务，发生 SQL 异常时自动回滚
-- 4. user 表字段默认按 has_phone / has_password / has_wechat 命名；若你的库里是驼峰列名，请自行替换

DROP PROCEDURE IF EXISTS sp_mock_users_and_addresses;
DROP PROCEDURE IF EXISTS sp_mock_orders_by_batch;
DROP PROCEDURE IF EXISTS sp_mock_reviews_by_batch;
DROP PROCEDURE IF EXISTS sp_clear_user_order_data_by_batch;

DELIMITER $$

CREATE PROCEDURE sp_mock_users_and_addresses(
    IN p_user_batch_no VARCHAR(32),
    IN p_user_count INT,
    IN p_address_per_user INT,
    IN p_center_lng DECIMAL(10,6),
    IN p_center_lat DECIMAL(10,6)
)
BEGIN
    DECLARE v_i INT DEFAULT 1;
    DECLARE v_j INT DEFAULT 1;
    DECLARE v_user_id BIGINT;
    DECLARE v_batch_code VARCHAR(32);
    DECLARE v_openid VARCHAR(64);
    DECLARE v_phone VARCHAR(11);
    DECLARE v_nickname VARCHAR(32);
    DECLARE v_avatar VARCHAR(255);
    DECLARE v_sex INT;
    DECLARE v_birthday DATE;
    DECLARE v_theta DOUBLE;
    DECLARE v_distance_km DOUBLE;
    DECLARE v_delta_lat DOUBLE;
    DECLARE v_delta_lng DOUBLE;
    DECLARE v_lng DECIMAL(10,6);
    DECLARE v_lat DECIMAL(10,6);
    DECLARE v_label VARCHAR(16);
    DECLARE v_detail VARCHAR(128);
    DECLARE v_consignee VARCHAR(32);
    DECLARE v_now DATETIME;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF p_user_batch_no IS NULL OR TRIM(p_user_batch_no) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '用户批次号不能为空';
    END IF;

    IF p_user_count IS NULL OR p_user_count <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '用户数量必须大于 0';
    END IF;

    IF p_address_per_user IS NULL OR p_address_per_user <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '每用户地址数必须大于 0';
    END IF;

    SET v_batch_code = UPPER(REPLACE(TRIM(p_user_batch_no), '-', ''));
    SET v_now = NOW();
    SET v_avatar = 'https://dummyimage.com/200x200/60a5fa/ffffff&text=User';

    IF EXISTS (
        SELECT 1
        FROM `user`
        WHERE LEFT(CAST(openid AS BINARY), CHAR_LENGTH(CONCAT('UB', v_batch_code)))
              = CAST(CONCAT('UB', v_batch_code) AS BINARY)
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '当前用户批次已存在，请更换批次号或先清理';
    END IF;

    START TRANSACTION;

    WHILE v_i <= p_user_count DO
        SET v_openid = CONCAT('UB', v_batch_code, LPAD(v_i, 4, '0'));
        SET v_phone = CONCAT('15', LPAD(MOD(CRC32(CONCAT(v_batch_code, '-phone-', v_i)), 1000000000), 9, '0'));
        SET v_nickname = CONCAT('测试用户', LPAD(v_i, 3, '0'));
        SET v_sex = IF(RAND() < 0.5, 1, 2);
        SET v_birthday = DATE_SUB(CURDATE(), INTERVAL (18 + FLOOR(RAND() * 22)) * 365 DAY);

        INSERT INTO `user` (
            openid,
            phone,
            password,
            nickname,
            avatar,
            sex,
            birthday,
            status,
            create_time,
            update_time,
            has_phone,
            has_password,
            has_wechat
        ) VALUES (
            v_openid,
            v_phone,
            'e10adc3949ba59abbe56e057f20f883e',
            v_nickname,
            v_avatar,
            v_sex,
            v_birthday,
            1,
            v_now,
            v_now,
            1,
            1,
            0
        );

        SET v_user_id = LAST_INSERT_ID();
        SET v_consignee = CONCAT('收货人', LPAD(v_i, 3, '0'));
        SET v_j = 1;

        WHILE v_j <= p_address_per_user DO
            SET v_theta = RAND() * 2 * PI();
            SET v_distance_km = 1 + RAND() * 7;
            SET v_delta_lat = (v_distance_km / 111.32) * SIN(v_theta);
            SET v_delta_lng = (v_distance_km / (111.32 * COS(RADIANS(p_center_lat)))) * COS(v_theta);
            SET v_lng = ROUND(p_center_lng + v_delta_lng, 6);
            SET v_lat = ROUND(p_center_lat + v_delta_lat, 6);
            SET v_label = CASE MOD(v_j, 3)
                WHEN 1 THEN '家'
                WHEN 2 THEN '公司'
                ELSE '学校'
            END;
            SET v_detail = CONCAT('测试地址-', v_batch_code, '-', LPAD(v_i, 3, '0'), '-', LPAD(v_j, 2, '0'), '号');

            INSERT INTO address_book (
                user_id,
                consignee,
                sex,
                phone,
                province_code,
                province_name,
                city_code,
                city_name,
                district_code,
                district_name,
                detail,
                label,
                longitude,
                latitude,
                is_default,
                create_time,
                update_time
            ) VALUES (
                v_user_id,
                v_consignee,
                v_sex,
                v_phone,
                '320000',
                '江苏省',
                '321000',
                '扬州市',
                '321003',
                '邗江区',
                v_detail,
                v_label,
                v_lng,
                v_lat,
                IF(v_j = 1, 1, 0),
                v_now,
                v_now
            );

            SET v_j = v_j + 1;
        END WHILE;

        SET v_i = v_i + 1;
    END WHILE;

    COMMIT;
END$$

CREATE PROCEDURE sp_mock_orders_by_batch(
    IN p_merchant_batch_no VARCHAR(32),
    IN p_user_batch_no VARCHAR(32),
    IN p_order_count INT
)
BEGIN
    DECLARE v_i INT DEFAULT 1;
    DECLARE v_item_index INT DEFAULT 1;
    DECLARE v_item_count INT DEFAULT 0;
    DECLARE v_qty INT DEFAULT 1;
    DECLARE v_merchant_batch_code VARCHAR(32);
    DECLARE v_user_batch_code VARCHAR(32);
    DECLARE v_order_prefix VARCHAR(24);
    DECLARE v_order_number VARCHAR(32);
    DECLARE v_order_id BIGINT;
    DECLARE v_user_id BIGINT;
    DECLARE v_user_nickname VARCHAR(64);
    DECLARE v_user_phone VARCHAR(16);
    DECLARE v_address_book_id BIGINT;
    DECLARE v_consignee VARCHAR(64);
    DECLARE v_full_address VARCHAR(255);
    DECLARE v_merchant_id BIGINT;
    DECLARE v_delivery_fee DECIMAL(10,2);
    DECLARE v_commission_rate DECIMAL(10,2);
    DECLARE v_item_dish_id BIGINT;
    DECLARE v_item_setmeal_id BIGINT;
    DECLARE v_item_name VARCHAR(128);
    DECLARE v_item_image VARCHAR(255);
    DECLARE v_item_flavor VARCHAR(255);
    DECLARE v_item_price DECIMAL(10,2);
    DECLARE v_amount DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_pack_amount DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_platform_commission DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_merchant_amount DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_total_amount DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_status_rand DOUBLE;
    DECLARE v_status INT;
    DECLARE v_pay_status INT;
    DECLARE v_pay_method INT;
    DECLARE v_order_time DATETIME;
    DECLARE v_checkout_time DATETIME;
    DECLARE v_estimated_delivery_time DATETIME;
    DECLARE v_delivery_time DATETIME;
    DECLARE v_cancel_time DATETIME;
    DECLARE v_cancel_reason VARCHAR(255);
    DECLARE v_has_dish INT DEFAULT 0;
    DECLARE v_has_setmeal INT DEFAULT 0;
    DECLARE v_now DATETIME;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF p_merchant_batch_no IS NULL OR TRIM(p_merchant_batch_no) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '商户批次号不能为空';
    END IF;

    IF p_user_batch_no IS NULL OR TRIM(p_user_batch_no) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '用户批次号不能为空';
    END IF;

    IF p_order_count IS NULL OR p_order_count <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '订单数量必须大于 0';
    END IF;

    SET v_merchant_batch_code = UPPER(REPLACE(TRIM(p_merchant_batch_no), '-', ''));
    SET v_user_batch_code = UPPER(REPLACE(TRIM(p_user_batch_no), '-', ''));
    SET v_order_prefix = CONCAT('OB', LEFT(v_merchant_batch_code, 8), LEFT(v_user_batch_code, 8));
    SET v_now = NOW();

    IF NOT EXISTS (
        SELECT 1 FROM merchant
        WHERE LEFT(CAST(merchant_code AS BINARY), CHAR_LENGTH(v_merchant_batch_code))
              = CAST(v_merchant_batch_code AS BINARY)
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '未找到对应商户批次';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM `user`
        WHERE LEFT(CAST(openid AS BINARY), CHAR_LENGTH(CONCAT('UB', v_user_batch_code)))
              = CAST(CONCAT('UB', v_user_batch_code) AS BINARY)
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '未找到对应用户批次';
    END IF;

    IF EXISTS (
        SELECT 1 FROM orders
        WHERE LEFT(CAST(order_number AS BINARY), CHAR_LENGTH(v_order_prefix))
              = CAST(v_order_prefix AS BINARY)
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '当前订单批次已存在，请先清理';
    END IF;

    START TRANSACTION;

    WHILE v_i <= p_order_count DO
        SELECT u.id, u.nickname, u.phone
        INTO v_user_id, v_user_nickname, v_user_phone
        FROM `user` u
        WHERE LEFT(CAST(u.openid AS BINARY), CHAR_LENGTH(CONCAT('UB', v_user_batch_code)))
              = CAST(CONCAT('UB', v_user_batch_code) AS BINARY)
        ORDER BY RAND()
        LIMIT 1;

        SELECT a.id,
               a.consignee,
               CONCAT(a.province_name, a.city_name, a.district_name, a.detail)
        INTO v_address_book_id, v_consignee, v_full_address
        FROM address_book a
        WHERE a.user_id = v_user_id
        ORDER BY a.is_default DESC, RAND()
        LIMIT 1;

        SELECT m.id,
               m.delivery_fee,
               m.commission_rate
        INTO v_merchant_id, v_delivery_fee, v_commission_rate
        FROM merchant m
        WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(v_merchant_batch_code))
              = CAST(v_merchant_batch_code AS BINARY)
          AND m.audit_status = 1
          AND m.status IN (1, 2)
        ORDER BY RAND()
        LIMIT 1;

        SET v_order_number = CONCAT(v_order_prefix, LPAD(v_i, 6, '0'));
        SET v_order_time = DATE_SUB(v_now, INTERVAL FLOOR(RAND() * 43200) MINUTE);
        SET v_pay_method = IF(RAND() < 0.5, 1, 2);
        SET v_status_rand = RAND();
        SET v_checkout_time = NULL;
        SET v_estimated_delivery_time = NULL;
        SET v_delivery_time = NULL;
        SET v_cancel_time = NULL;
        SET v_cancel_reason = NULL;

        IF v_status_rand < 0.10 THEN
            SET v_status = 1;
            SET v_pay_status = 0;
        ELSEIF v_status_rand < 0.20 THEN
            SET v_status = 2;
            SET v_pay_status = 1;
            SET v_checkout_time = DATE_ADD(v_order_time, INTERVAL 5 MINUTE);
        ELSEIF v_status_rand < 0.30 THEN
            SET v_status = 3;
            SET v_pay_status = 1;
            SET v_checkout_time = DATE_ADD(v_order_time, INTERVAL 5 MINUTE);
            SET v_estimated_delivery_time = DATE_ADD(v_order_time, INTERVAL 45 MINUTE);
        ELSEIF v_status_rand < 0.40 THEN
            SET v_status = 4;
            SET v_pay_status = 1;
            SET v_checkout_time = DATE_ADD(v_order_time, INTERVAL 5 MINUTE);
            SET v_estimated_delivery_time = DATE_ADD(v_order_time, INTERVAL 45 MINUTE);
        ELSEIF v_status_rand < 0.95 THEN
            SET v_status = 5;
            SET v_pay_status = 1;
            SET v_checkout_time = DATE_ADD(v_order_time, INTERVAL 5 MINUTE);
            SET v_estimated_delivery_time = DATE_ADD(v_order_time, INTERVAL 45 MINUTE);
            SET v_delivery_time = DATE_ADD(v_order_time, INTERVAL (35 + FLOOR(RAND() * 30)) MINUTE);
        ELSE
            SET v_status = 6;
            SET v_pay_status = 1;
            SET v_checkout_time = DATE_ADD(v_order_time, INTERVAL 5 MINUTE);
            SET v_cancel_time = DATE_ADD(v_order_time, INTERVAL 12 MINUTE);
            SET v_cancel_reason = '用户临时取消';
        END IF;

        INSERT INTO orders (
            order_number,
            user_id,
            merchant_id,
            address_book_id,
            status,
            order_time,
            checkout_time,
            pay_method,
            pay_status,
            amount,
            delivery_fee,
            pack_amount,
            platform_commission,
            merchant_amount,
            total_amount,
            remark,
            phone,
            address,
            consignee,
            user_name,
            cancel_reason,
            rejection_reason,
            cancel_time,
            estimated_delivery_time,
            delivery_time,
            tableware_number,
            create_time,
            update_time
        ) VALUES (
            v_order_number,
            v_user_id,
            v_merchant_id,
            v_address_book_id,
            v_status,
            v_order_time,
            v_checkout_time,
            v_pay_method,
            v_pay_status,
            0,
            IFNULL(v_delivery_fee, 0),
            0,
            0,
            0,
            0,
            '批量测试订单',
            v_user_phone,
            v_full_address,
            v_consignee,
            v_consignee,
            v_cancel_reason,
            NULL,
            v_cancel_time,
            v_estimated_delivery_time,
            v_delivery_time,
            1 + FLOOR(RAND() * 3),
            v_order_time,
            v_order_time
        );

        SET v_order_id = LAST_INSERT_ID();
        SET v_item_count = 2 + FLOOR(RAND() * 4);
        SET v_item_index = 1;
        SET v_amount = 0.00;
        SET v_pack_amount = 0.00;

        SELECT COUNT(*) INTO v_has_dish
        FROM dish
        WHERE merchant_id = v_merchant_id AND status = 1;

        SELECT COUNT(*) INTO v_has_setmeal
        FROM setmeal
        WHERE merchant_id = v_merchant_id AND status = 1;

        WHILE v_item_index <= v_item_count DO
            SET v_qty = 1 + FLOOR(RAND() * 2);
            SET v_item_dish_id = NULL;
            SET v_item_setmeal_id = NULL;
            SET v_item_flavor = NULL;

            IF v_has_setmeal > 0 AND (v_has_dish = 0 OR RAND() < 0.25) THEN
                SELECT s.id, s.name, s.image, s.price
                INTO v_item_setmeal_id, v_item_name, v_item_image, v_item_price
                FROM setmeal s
                WHERE s.merchant_id = v_merchant_id AND s.status = 1
                ORDER BY RAND()
                LIMIT 1;
            ELSE
                SELECT d.id, d.name, d.image, d.price
                INTO v_item_dish_id, v_item_name, v_item_image, v_item_price
                FROM dish d
                WHERE d.merchant_id = v_merchant_id AND d.status = 1
                ORDER BY RAND()
                LIMIT 1;

                SET v_item_flavor = (
                    SELECT df.value
                    FROM dish_flavor df
                    WHERE df.dish_id = v_item_dish_id
                    ORDER BY RAND()
                    LIMIT 1
                );
            END IF;

            INSERT INTO order_detail (
                order_id,
                dish_id,
                setmeal_id,
                name,
                image,
                dish_flavor,
                number,
                amount
            ) VALUES (
                v_order_id,
                v_item_dish_id,
                v_item_setmeal_id,
                v_item_name,
                v_item_image,
                v_item_flavor,
                v_qty,
                v_item_price
            );

            SET v_amount = v_amount + (v_item_price * v_qty);
            SET v_pack_amount = v_pack_amount + 1.00;
            SET v_item_index = v_item_index + 1;
        END WHILE;

        SET v_platform_commission = ROUND(v_amount * IFNULL(v_commission_rate, 18.00) / 100, 2);
        SET v_merchant_amount = ROUND(v_amount - v_platform_commission, 2);
        SET v_total_amount = ROUND(v_amount + IFNULL(v_delivery_fee, 0) + v_pack_amount, 2);

        UPDATE orders
        SET amount = ROUND(v_amount, 2),
            delivery_fee = IFNULL(v_delivery_fee, 0),
            pack_amount = ROUND(v_pack_amount, 2),
            platform_commission = v_platform_commission,
            merchant_amount = v_merchant_amount,
            total_amount = v_total_amount,
            update_time = v_now
        WHERE id = v_order_id;

        SET v_i = v_i + 1;
    END WHILE;

    COMMIT;
END$$

CREATE PROCEDURE sp_mock_reviews_by_batch(
    IN p_merchant_batch_no VARCHAR(32),
    IN p_user_batch_no VARCHAR(32),
    IN p_review_ratio DECIMAL(5,2)
)
BEGIN
    DECLARE v_done INT DEFAULT 0;
    DECLARE v_order_id BIGINT;
    DECLARE v_user_id BIGINT;
    DECLARE v_merchant_id BIGINT;
    DECLARE v_base_time DATETIME;
    DECLARE v_merchant_batch_code VARCHAR(32);
    DECLARE v_user_batch_code VARCHAR(32);
    DECLARE v_ratio DECIMAL(5,2);
    DECLARE v_rating INT;
    DECLARE v_content VARCHAR(255);
    DECLARE v_images VARCHAR(500);
    DECLARE v_reply VARCHAR(255);
    DECLARE v_reply_time DATETIME;
    DECLARE v_create_time DATETIME;
    DECLARE v_rand DOUBLE;

    DECLARE cur_review CURSOR FOR
        SELECT o.id,
               o.user_id,
               o.merchant_id,
               IFNULL(o.delivery_time, IFNULL(o.checkout_time, o.order_time)) AS base_time
        FROM orders o
        JOIN merchant m ON m.id = o.merchant_id
        JOIN `user` u ON u.id = o.user_id
        WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(v_merchant_batch_code))
              = CAST(v_merchant_batch_code AS BINARY)
          AND LEFT(CAST(u.openid AS BINARY), CHAR_LENGTH(CONCAT('UB', v_user_batch_code)))
              = CAST(CONCAT('UB', v_user_batch_code) AS BINARY)
          AND o.status = 5
          AND o.pay_status = 1
          AND NOT EXISTS (
              SELECT 1 FROM order_review r WHERE r.order_id = o.id
          )
        ORDER BY o.id;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = 1;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF p_merchant_batch_no IS NULL OR TRIM(p_merchant_batch_no) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '商户批次号不能为空';
    END IF;

    IF p_user_batch_no IS NULL OR TRIM(p_user_batch_no) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '用户批次号不能为空';
    END IF;

    IF p_review_ratio IS NULL OR p_review_ratio <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '评价覆盖率必须大于 0';
    END IF;

    SET v_merchant_batch_code = UPPER(REPLACE(TRIM(p_merchant_batch_no), '-', ''));
    SET v_user_batch_code = UPPER(REPLACE(TRIM(p_user_batch_no), '-', ''));
    SET v_ratio = IF(p_review_ratio > 1, p_review_ratio / 100, p_review_ratio);

    START TRANSACTION;

    OPEN cur_review;

    review_loop: LOOP
        FETCH cur_review INTO v_order_id, v_user_id, v_merchant_id, v_base_time;
        IF v_done = 1 THEN
            LEAVE review_loop;
        END IF;

        IF RAND() <= v_ratio THEN
            SET v_rand = RAND();

            IF v_rand < 0.35 THEN
                SET v_rating = 5;
                SET v_content = '配送及时，口味稳定，会继续回购';
            ELSEIF v_rand < 0.70 THEN
                SET v_rating = 4;
                SET v_content = '整体不错，分量合适，体验较好';
            ELSEIF v_rand < 0.85 THEN
                SET v_rating = 3;
                SET v_content = '中规中矩，口味正常，包装还可以';
            ELSEIF v_rand < 0.95 THEN
                SET v_rating = 2;
                SET v_content = '送达稍慢，口感一般，希望改进';
            ELSE
                SET v_rating = 1;
                SET v_content = '本次体验较差，和预期有差距';
            END IF;

            SET v_images = NULL;
            IF RAND() < 0.20 THEN
                SET v_images = 'https://dummyimage.com/300x300/f97316/ffffff&text=Review';
            END IF;

            SET v_reply = NULL;
            SET v_reply_time = NULL;
            IF RAND() < 0.30 THEN
                SET v_reply = '感谢反馈，我们会持续优化服务与口味';
                SET v_reply_time = DATE_ADD(v_base_time, INTERVAL (2 + FLOOR(RAND() * 48)) HOUR);
            END IF;

            SET v_create_time = DATE_ADD(v_base_time, INTERVAL (1 + FLOOR(RAND() * 72)) HOUR);

            INSERT INTO order_review (
                order_id,
                user_id,
                merchant_id,
                rating,
                content,
                images,
                merchant_reply,
                reply_time,
                create_time,
                update_time
            ) VALUES (
                v_order_id,
                v_user_id,
                v_merchant_id,
                v_rating,
                v_content,
                v_images,
                v_reply,
                v_reply_time,
                v_create_time,
                v_create_time
            );
        END IF;
    END LOOP;

    CLOSE cur_review;

    COMMIT;
END$$

CREATE PROCEDURE sp_clear_user_order_data_by_batch(
    IN p_merchant_batch_no VARCHAR(32),
    IN p_user_batch_no VARCHAR(32)
)
BEGIN
    DECLARE v_merchant_batch_code VARCHAR(32);
    DECLARE v_user_batch_code VARCHAR(32);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF p_merchant_batch_no IS NULL OR TRIM(p_merchant_batch_no) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '商户批次号不能为空';
    END IF;

    IF p_user_batch_no IS NULL OR TRIM(p_user_batch_no) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '用户批次号不能为空';
    END IF;

    SET v_merchant_batch_code = UPPER(REPLACE(TRIM(p_merchant_batch_no), '-', ''));
    SET v_user_batch_code = UPPER(REPLACE(TRIM(p_user_batch_no), '-', ''));

    START TRANSACTION;

    DELETE sc
    FROM shopping_cart sc
    JOIN `user` u ON u.id = sc.user_id
    WHERE LEFT(CAST(u.openid AS BINARY), CHAR_LENGTH(CONCAT('UB', v_user_batch_code)))
          = CAST(CONCAT('UB', v_user_batch_code) AS BINARY);

    DELETE r
    FROM order_review r
    JOIN orders o ON o.id = r.order_id
    JOIN merchant m ON m.id = o.merchant_id
    JOIN `user` u ON u.id = o.user_id
    WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(v_merchant_batch_code))
          = CAST(v_merchant_batch_code AS BINARY)
      AND LEFT(CAST(u.openid AS BINARY), CHAR_LENGTH(CONCAT('UB', v_user_batch_code)))
          = CAST(CONCAT('UB', v_user_batch_code) AS BINARY);

    DELETE od
    FROM order_detail od
    JOIN orders o ON o.id = od.order_id
    JOIN merchant m ON m.id = o.merchant_id
    JOIN `user` u ON u.id = o.user_id
    WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(v_merchant_batch_code))
          = CAST(v_merchant_batch_code AS BINARY)
      AND LEFT(CAST(u.openid AS BINARY), CHAR_LENGTH(CONCAT('UB', v_user_batch_code)))
          = CAST(CONCAT('UB', v_user_batch_code) AS BINARY);

    DELETE o
    FROM orders o
    JOIN merchant m ON m.id = o.merchant_id
    JOIN `user` u ON u.id = o.user_id
    WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(v_merchant_batch_code))
          = CAST(v_merchant_batch_code AS BINARY)
      AND LEFT(CAST(u.openid AS BINARY), CHAR_LENGTH(CONCAT('UB', v_user_batch_code)))
          = CAST(CONCAT('UB', v_user_batch_code) AS BINARY);

    DELETE a
    FROM address_book a
    JOIN `user` u ON u.id = a.user_id
    WHERE LEFT(CAST(u.openid AS BINARY), CHAR_LENGTH(CONCAT('UB', v_user_batch_code)))
          = CAST(CONCAT('UB', v_user_batch_code) AS BINARY);

    DELETE FROM `user`
    WHERE LEFT(CAST(openid AS BINARY), CHAR_LENGTH(CONCAT('UB', v_user_batch_code)))
          = CAST(CONCAT('UB', v_user_batch_code) AS BINARY);

    COMMIT;
END$$

DELIMITER ;

-- =========================
-- 示例执行顺序
-- =========================
-- 1. 生成用户与地址
-- CALL sp_mock_users_and_addresses('U250425A', 40, 2, 119.943466, 32.464328);
--
-- 2. 生成订单与明细
-- CALL sp_mock_orders_by_batch('T250425A', 'U250425A', 600);
--
-- 3. 生成评价
-- CALL sp_mock_reviews_by_batch('T250425A', 'U250425A', 0.60);
--
-- 4. 如需清理这批用户/订单/评价数据
-- CALL sp_clear_user_order_data_by_batch('T250425A', 'U250425A');
--
-- =========================
-- 快速校验 SQL
-- =========================
-- SET @merchant_batch = 'T250425A';
-- SET @user_batch = 'U250425A';
--
-- SELECT COUNT(*) AS user_count
-- FROM `user`
-- WHERE LEFT(CAST(openid AS BINARY), CHAR_LENGTH(CONCAT('UB', UPPER(REPLACE(@user_batch, '-', '')))))
--       = CAST(CONCAT('UB', UPPER(REPLACE(@user_batch, '-', ''))) AS BINARY);
--
-- SELECT COUNT(*) AS order_count
-- FROM orders
-- WHERE LEFT(CAST(order_number AS BINARY), CHAR_LENGTH(CONCAT('OB', LEFT(UPPER(REPLACE(@merchant_batch, '-', '')), 8), LEFT(UPPER(REPLACE(@user_batch, '-', '')), 8))))
--       = CAST(CONCAT('OB', LEFT(UPPER(REPLACE(@merchant_batch, '-', '')), 8), LEFT(UPPER(REPLACE(@user_batch, '-', '')), 8)) AS BINARY);
--
-- SELECT status, COUNT(*) AS cnt
-- FROM orders
-- WHERE LEFT(CAST(order_number AS BINARY), CHAR_LENGTH(CONCAT('OB', LEFT(UPPER(REPLACE(@merchant_batch, '-', '')), 8), LEFT(UPPER(REPLACE(@user_batch, '-', '')), 8))))
--       = CAST(CONCAT('OB', LEFT(UPPER(REPLACE(@merchant_batch, '-', '')), 8), LEFT(UPPER(REPLACE(@user_batch, '-', '')), 8)) AS BINARY)
-- GROUP BY status
-- ORDER BY status;
--
-- SELECT COUNT(*) AS review_count
-- FROM order_review r
-- JOIN orders o ON o.id = r.order_id
-- WHERE LEFT(CAST(o.order_number AS BINARY), CHAR_LENGTH(CONCAT('OB', LEFT(UPPER(REPLACE(@merchant_batch, '-', '')), 8), LEFT(UPPER(REPLACE(@user_batch, '-', '')), 8))))
--       = CAST(CONCAT('OB', LEFT(UPPER(REPLACE(@merchant_batch, '-', '')), 8), LEFT(UPPER(REPLACE(@user_batch, '-', '')), 8)) AS BINARY);
