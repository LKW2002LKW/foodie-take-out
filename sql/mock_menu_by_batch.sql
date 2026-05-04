-- 菜单测试数据存储过程
-- 目标：
-- 1. 按商户批次生成分类、菜品、口味、套餐、套餐明细
-- 2. 提供独立的分类过程、菜单过程、清理过程
-- 3. 每个过程内部使用事务，发生 SQL 异常时自动回滚
--
-- 依赖前提：
-- 1. 需先执行 mock_merchants_nearby.sql，已有对应批次 merchant / merchant_admin 数据
-- 2. 相关表均为 InnoDB
-- 3. 本脚本只做 DML，存储过程内部事务可回滚

DROP PROCEDURE IF EXISTS sp_mock_categories_by_batch;
DROP PROCEDURE IF EXISTS sp_mock_menu_by_batch;
DROP PROCEDURE IF EXISTS sp_clear_menu_by_batch;

DELIMITER $$

CREATE PROCEDURE sp_mock_categories_by_batch(
    IN p_batch_no VARCHAR(32),
    IN p_create_user BIGINT
)
BEGIN
    DECLARE v_done INT DEFAULT 0;
    DECLARE v_merchant_id BIGINT;
    DECLARE v_biz_category_id BIGINT;
    DECLARE v_operator_id BIGINT DEFAULT 1;
    DECLARE v_batch_code VARCHAR(32);
    DECLARE v_now DATETIME;
    DECLARE v_existing_count INT DEFAULT 0;

    DECLARE cur_merchant CURSOR FOR
        SELECT id, biz_category_id
        FROM merchant
        WHERE LEFT(CAST(merchant_code AS BINARY), CHAR_LENGTH(v_batch_code))
              = CAST(v_batch_code AS BINARY)
        ORDER BY id;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = 1;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF p_batch_no IS NULL OR TRIM(p_batch_no) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '批次号不能为空';
    END IF;

    SET v_batch_code = UPPER(REPLACE(TRIM(p_batch_no), '-', ''));
    SET v_operator_id = IFNULL(p_create_user, 1);
    SET v_now = NOW();

    IF NOT EXISTS (
        SELECT 1
        FROM merchant
        WHERE LEFT(CAST(merchant_code AS BINARY), CHAR_LENGTH(v_batch_code))
              = CAST(v_batch_code AS BINARY)
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '未找到对应批次商户，请先生成商户数据';
    END IF;

    SELECT COUNT(*) INTO v_existing_count
    FROM category c
    JOIN merchant m ON m.id = c.merchant_id
    WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(v_batch_code))
          = CAST(v_batch_code AS BINARY);

    IF v_existing_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '当前批次已存在分类数据，请先清理';
    END IF;

    START TRANSACTION;

    OPEN cur_merchant;

    read_loop: LOOP
        FETCH cur_merchant INTO v_merchant_id, v_biz_category_id;
        IF v_done = 1 THEN
            LEAVE read_loop;
        END IF;

        CASE v_biz_category_id
            WHEN 1 THEN
                INSERT INTO category (merchant_id, name, type, sort, status, create_time, update_time, create_user, update_user)
                VALUES
                (v_merchant_id, '招牌主食', 1, 1, 1, v_now, v_now, v_operator_id, v_operator_id),
                (v_merchant_id, '热销小吃', 1, 2, 1, v_now, v_now, v_operator_id, v_operator_id),
                (v_merchant_id, '汤饮配餐', 1, 3, 1, v_now, v_now, v_operator_id, v_operator_id),
                (v_merchant_id, '超值套餐', 2, 4, 1, v_now, v_now, v_operator_id, v_operator_id);
            WHEN 2 THEN
                INSERT INTO category (merchant_id, name, type, sort, status, create_time, update_time, create_user, update_user)
                VALUES
                (v_merchant_id, '奶茶系列', 1, 1, 1, v_now, v_now, v_operator_id, v_operator_id),
                (v_merchant_id, '咖啡系列', 1, 2, 1, v_now, v_now, v_operator_id, v_operator_id),
                (v_merchant_id, '甜品系列', 1, 3, 1, v_now, v_now, v_operator_id, v_operator_id),
                (v_merchant_id, '双人下午茶', 2, 4, 1, v_now, v_now, v_operator_id, v_operator_id);
            WHEN 3 THEN
                INSERT INTO category (merchant_id, name, type, sort, status, create_time, update_time, create_user, update_user)
                VALUES
                (v_merchant_id, '休闲零食', 1, 1, 1, v_now, v_now, v_operator_id, v_operator_id),
                (v_merchant_id, '酒水饮料', 1, 2, 1, v_now, v_now, v_operator_id, v_operator_id),
                (v_merchant_id, '生活日用', 1, 3, 1, v_now, v_now, v_operator_id, v_operator_id),
                (v_merchant_id, '囤货组合', 2, 4, 1, v_now, v_now, v_operator_id, v_operator_id);
            ELSE
                INSERT INTO category (merchant_id, name, type, sort, status, create_time, update_time, create_user, update_user)
                VALUES
                (v_merchant_id, '时令水果', 1, 1, 1, v_now, v_now, v_operator_id, v_operator_id),
                (v_merchant_id, '新鲜蔬菜', 1, 2, 1, v_now, v_now, v_operator_id, v_operator_id),
                (v_merchant_id, '轻食净菜', 1, 3, 1, v_now, v_now, v_operator_id, v_operator_id),
                (v_merchant_id, '家庭组合', 2, 4, 1, v_now, v_now, v_operator_id, v_operator_id);
        END CASE;
    END LOOP;

    CLOSE cur_merchant;

    COMMIT;
END$$

CREATE PROCEDURE sp_mock_menu_by_batch(
    IN p_batch_no VARCHAR(32),
    IN p_create_user BIGINT,
    IN p_dish_per_merchant INT,
    IN p_setmeal_per_merchant INT
)
BEGIN
    DECLARE v_done INT DEFAULT 0;
    DECLARE v_i INT;
    DECLARE v_j INT;
    DECLARE v_pick INT;
    DECLARE v_merchant_id BIGINT;
    DECLARE v_biz_category_id BIGINT;
    DECLARE v_category1_id BIGINT;
    DECLARE v_category2_id BIGINT;
    DECLARE v_category3_id BIGINT;
    DECLARE v_setmeal_category_id BIGINT;
    DECLARE v_target_category_id BIGINT;
    DECLARE v_dish_id BIGINT;
    DECLARE v_setmeal_id BIGINT;
    DECLARE v_operator_id BIGINT DEFAULT 1;
    DECLARE v_batch_code VARCHAR(32);
    DECLARE v_now DATETIME;
    DECLARE v_existing_dish_count INT DEFAULT 0;
    DECLARE v_existing_setmeal_count INT DEFAULT 0;
    DECLARE v_dish_name VARCHAR(64);
    DECLARE v_dish_desc VARCHAR(255);
    DECLARE v_dish_image VARCHAR(255);
    DECLARE v_dish_price DECIMAL(10,2);
    DECLARE v_setmeal_name VARCHAR(64);
    DECLARE v_setmeal_desc VARCHAR(255);
    DECLARE v_setmeal_image VARCHAR(255);
    DECLARE v_setmeal_price DECIMAL(10,2);
    DECLARE v_flavor_name1 VARCHAR(32);
    DECLARE v_flavor_value1 VARCHAR(255);
    DECLARE v_flavor_name2 VARCHAR(32);
    DECLARE v_flavor_value2 VARCHAR(255);
    DECLARE v_cnt_cat1 INT DEFAULT 0;
    DECLARE v_cnt_cat2 INT DEFAULT 0;
    DECLARE v_cnt_cat3 INT DEFAULT 0;
    DECLARE v_dish_count_each INT DEFAULT 0;
    DECLARE v_price_sum DECIMAL(10,2);

    DECLARE cur_merchant CURSOR FOR
        SELECT m.id, m.biz_category_id,
               MAX(CASE WHEN c.type = 1 AND c.sort = 1 THEN c.id END) AS category1_id,
               MAX(CASE WHEN c.type = 1 AND c.sort = 2 THEN c.id END) AS category2_id,
               MAX(CASE WHEN c.type = 1 AND c.sort = 3 THEN c.id END) AS category3_id,
               MAX(CASE WHEN c.type = 2 AND c.sort = 4 THEN c.id END) AS setmeal_category_id
        FROM merchant m
        LEFT JOIN category c ON c.merchant_id = m.id AND c.status = 1
        WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(v_batch_code))
              = CAST(v_batch_code AS BINARY)
        GROUP BY m.id, m.biz_category_id
        ORDER BY m.id;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = 1;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF p_batch_no IS NULL OR TRIM(p_batch_no) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '批次号不能为空';
    END IF;

    IF p_dish_per_merchant IS NULL OR p_dish_per_merchant < 3 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '每商户菜品数不能小于 3';
    END IF;

    IF p_setmeal_per_merchant IS NULL OR p_setmeal_per_merchant < 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '每商户套餐数不能小于 1';
    END IF;

    SET v_batch_code = UPPER(REPLACE(TRIM(p_batch_no), '-', ''));
    SET v_operator_id = IFNULL(p_create_user, 1);
    SET v_now = NOW();
    SET v_dish_image = 'https://dummyimage.com/300x300/f59e0b/ffffff&text=Dish';
    SET v_setmeal_image = 'https://dummyimage.com/300x300/10b981/ffffff&text=Setmeal';

    IF NOT EXISTS (
        SELECT 1
        FROM category c
        JOIN merchant m ON m.id = c.merchant_id
        WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(v_batch_code))
              = CAST(v_batch_code AS BINARY)
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '当前批次未生成分类数据，请先执行 sp_mock_categories_by_batch';
    END IF;

    SELECT COUNT(*) INTO v_existing_dish_count
    FROM dish d
    JOIN merchant m ON m.id = d.merchant_id
    WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(v_batch_code))
          = CAST(v_batch_code AS BINARY);

    SELECT COUNT(*) INTO v_existing_setmeal_count
    FROM setmeal s
    JOIN merchant m ON m.id = s.merchant_id
    WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(v_batch_code))
          = CAST(v_batch_code AS BINARY);

    IF v_existing_dish_count > 0 OR v_existing_setmeal_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '当前批次已存在菜单数据，请先清理';
    END IF;

    START TRANSACTION;

    OPEN cur_merchant;

    merchant_loop: LOOP
        FETCH cur_merchant INTO v_merchant_id, v_biz_category_id, v_category1_id, v_category2_id, v_category3_id, v_setmeal_category_id;
        IF v_done = 1 THEN
            LEAVE merchant_loop;
        END IF;

        IF v_category1_id IS NULL OR v_category2_id IS NULL OR v_category3_id IS NULL OR v_setmeal_category_id IS NULL THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '分类数据不完整，无法生成菜单';
        END IF;

        SET v_cnt_cat1 = FLOOR(p_dish_per_merchant / 3);
        SET v_cnt_cat2 = FLOOR(p_dish_per_merchant / 3);
        SET v_cnt_cat3 = p_dish_per_merchant - v_cnt_cat1 - v_cnt_cat2;
        SET v_i = 1;

        WHILE v_i <= p_dish_per_merchant DO
            IF v_i <= v_cnt_cat1 THEN
                SET v_target_category_id = v_category1_id;
            ELSEIF v_i <= v_cnt_cat1 + v_cnt_cat2 THEN
                SET v_target_category_id = v_category2_id;
            ELSE
                SET v_target_category_id = v_category3_id;
            END IF;

            CASE v_biz_category_id
                WHEN 1 THEN
                    CASE MOD(v_i - 1, 6)
                        WHEN 0 THEN SET v_dish_name = CONCAT('招牌黄焖鸡', LPAD(v_i, 2, '0')), v_dish_desc = '鸡肉入味，下饭首选', v_dish_price = ROUND(16 + RAND() * 8, 2), v_flavor_name1 = '辣度', v_flavor_value1 = '["不辣","微辣","中辣","重辣"]', v_flavor_name2 = '加料', v_flavor_value2 = '["加蛋","加肠","加饭"]';
                        WHEN 1 THEN SET v_dish_name = CONCAT('香菇滑鸡饭', LPAD(v_i, 2, '0')), v_dish_desc = '米饭粒粒分明，口感稳定', v_dish_price = ROUND(14 + RAND() * 7, 2), v_flavor_name1 = '分量', v_flavor_value1 = '["标准","加量"]', v_flavor_name2 = NULL, v_flavor_value2 = NULL;
                        WHEN 2 THEN SET v_dish_name = CONCAT('老坛酸菜面', LPAD(v_i, 2, '0')), v_dish_desc = '酸爽开胃，汤底浓郁', v_dish_price = ROUND(13 + RAND() * 6, 2), v_flavor_name1 = '辣度', v_flavor_value1 = '["不辣","微辣","中辣"]', v_flavor_name2 = '加料', v_flavor_value2 = '["加蛋","加丸子"]';
                        WHEN 3 THEN SET v_dish_name = CONCAT('脆皮鸡排', LPAD(v_i, 2, '0')), v_dish_desc = '外酥里嫩，现炸现卖', v_dish_price = ROUND(10 + RAND() * 6, 2), v_flavor_name1 = '口味', v_flavor_value1 = '["原味","孜然","香辣"]', v_flavor_name2 = NULL, v_flavor_value2 = NULL;
                        WHEN 4 THEN SET v_dish_name = CONCAT('紫菜蛋花汤', LPAD(v_i, 2, '0')), v_dish_desc = '清爽解腻，适合配餐', v_dish_price = ROUND(6 + RAND() * 4, 2), v_flavor_name1 = NULL, v_flavor_value1 = NULL, v_flavor_name2 = NULL, v_flavor_value2 = NULL;
                        ELSE SET v_dish_name = CONCAT('可乐鸡翅', LPAD(v_i, 2, '0')), v_dish_desc = '甜咸平衡，适合大众口味', v_dish_price = ROUND(12 + RAND() * 8, 2), v_flavor_name1 = '份量', v_flavor_value1 = '["小份","大份"]', v_flavor_name2 = NULL, v_flavor_value2 = NULL;
                    END CASE;
                WHEN 2 THEN
                    CASE MOD(v_i - 1, 6)
                        WHEN 0 THEN SET v_dish_name = CONCAT('杨枝甘露', LPAD(v_i, 2, '0')), v_dish_desc = '果香浓郁，清爽不腻', v_dish_price = ROUND(15 + RAND() * 8, 2), v_flavor_name1 = '甜度', v_flavor_value1 = '["无糖","少糖","半糖","标准糖"]', v_flavor_name2 = '温度', v_flavor_value2 = '["去冰","少冰","常温"]';
                        WHEN 1 THEN SET v_dish_name = CONCAT('生椰拿铁', LPAD(v_i, 2, '0')), v_dish_desc = '咖啡香气明显，椰乳顺滑', v_dish_price = ROUND(16 + RAND() * 10, 2), v_flavor_name1 = '冰量', v_flavor_value1 = '["去冰","少冰","标准冰"]', v_flavor_name2 = '甜度', v_flavor_value2 = '["无糖","少糖","标准糖"]';
                        WHEN 2 THEN SET v_dish_name = CONCAT('爆浆蛋挞', LPAD(v_i, 2, '0')), v_dish_desc = '酥皮层次分明，奶香浓郁', v_dish_price = ROUND(8 + RAND() * 6, 2), v_flavor_name1 = '规格', v_flavor_value1 = '["单只","双只","四只"]', v_flavor_name2 = NULL, v_flavor_value2 = NULL;
                        WHEN 3 THEN SET v_dish_name = CONCAT('芝士奶盖茶', LPAD(v_i, 2, '0')), v_dish_desc = '茶底清爽，奶盖绵密', v_dish_price = ROUND(14 + RAND() * 8, 2), v_flavor_name1 = '甜度', v_flavor_value1 = '["少糖","半糖","标准糖"]', v_flavor_name2 = '温度', v_flavor_value2 = '["去冰","少冰","热"]';
                        WHEN 4 THEN SET v_dish_name = CONCAT('提拉米苏杯', LPAD(v_i, 2, '0')), v_dish_desc = '口感丰富，适合下午茶', v_dish_price = ROUND(12 + RAND() * 8, 2), v_flavor_name1 = NULL, v_flavor_value1 = NULL, v_flavor_name2 = NULL, v_flavor_value2 = NULL;
                        ELSE SET v_dish_name = CONCAT('美式咖啡', LPAD(v_i, 2, '0')), v_dish_desc = '风味干净，提神清爽', v_dish_price = ROUND(10 + RAND() * 6, 2), v_flavor_name1 = '温度', v_flavor_value1 = '["冰","热"]', v_flavor_name2 = '规格', v_flavor_value2 = '["中杯","大杯"]';
                    END CASE;
                WHEN 3 THEN
                    CASE MOD(v_i - 1, 6)
                        WHEN 0 THEN SET v_dish_name = CONCAT('薯片组合', LPAD(v_i, 2, '0')), v_dish_desc = '多口味零食组合，适合囤货', v_dish_price = ROUND(12 + RAND() * 10, 2), v_flavor_name1 = '规格', v_flavor_value1 = '["小份","中份","大份"]', v_flavor_name2 = NULL, v_flavor_value2 = NULL;
                        WHEN 1 THEN SET v_dish_name = CONCAT('可乐整箱', LPAD(v_i, 2, '0')), v_dish_desc = '家庭聚会常备饮品', v_dish_price = ROUND(18 + RAND() * 16, 2), v_flavor_name1 = '规格', v_flavor_value1 = '["6罐装","12罐装"]', v_flavor_name2 = NULL, v_flavor_value2 = NULL;
                        WHEN 2 THEN SET v_dish_name = CONCAT('抽纸三包装', LPAD(v_i, 2, '0')), v_dish_desc = '日常消耗品，家庭必备', v_dish_price = ROUND(9 + RAND() * 8, 2), v_flavor_name1 = NULL, v_flavor_value1 = NULL, v_flavor_name2 = NULL, v_flavor_value2 = NULL;
                        WHEN 3 THEN SET v_dish_name = CONCAT('矿泉水整提', LPAD(v_i, 2, '0')), v_dish_desc = '便捷取用，办公室常备', v_dish_price = ROUND(10 + RAND() * 8, 2), v_flavor_name1 = '规格', v_flavor_value1 = '["12瓶","24瓶"]', v_flavor_name2 = NULL, v_flavor_value2 = NULL;
                        WHEN 4 THEN SET v_dish_name = CONCAT('洗衣液补充装', LPAD(v_i, 2, '0')), v_dish_desc = '日用清洁补货方便', v_dish_price = ROUND(20 + RAND() * 18, 2), v_flavor_name1 = '规格', v_flavor_value1 = '["500g","1kg"]', v_flavor_name2 = NULL, v_flavor_value2 = NULL;
                        ELSE SET v_dish_name = CONCAT('泡面组合', LPAD(v_i, 2, '0')), v_dish_desc = '夜宵常备，开箱即食', v_dish_price = ROUND(14 + RAND() * 12, 2), v_flavor_name1 = '口味', v_flavor_value1 = '["红烧","酸菜","藤椒"]', v_flavor_name2 = NULL, v_flavor_value2 = NULL;
                    END CASE;
                ELSE
                    CASE MOD(v_i - 1, 6)
                        WHEN 0 THEN SET v_dish_name = CONCAT('红富士苹果', LPAD(v_i, 2, '0')), v_dish_desc = '脆甜多汁，适合全家食用', v_dish_price = ROUND(8 + RAND() * 10, 2), v_flavor_name1 = '规格', v_flavor_value1 = '["500g","1000g","1500g"]', v_flavor_name2 = NULL, v_flavor_value2 = NULL;
                        WHEN 1 THEN SET v_dish_name = CONCAT('海南香蕉', LPAD(v_i, 2, '0')), v_dish_desc = '成熟度适中，口感绵甜', v_dish_price = ROUND(6 + RAND() * 8, 2), v_flavor_name1 = '规格', v_flavor_value1 = '["500g","1000g"]', v_flavor_name2 = NULL, v_flavor_value2 = NULL;
                        WHEN 2 THEN SET v_dish_name = CONCAT('嫩菠菜', LPAD(v_i, 2, '0')), v_dish_desc = '叶片鲜嫩，适合清炒打汤', v_dish_price = ROUND(5 + RAND() * 6, 2), v_flavor_name1 = '规格', v_flavor_value1 = '["250g","500g","1000g"]', v_flavor_name2 = NULL, v_flavor_value2 = NULL;
                        WHEN 3 THEN SET v_dish_name = CONCAT('净菜沙拉包', LPAD(v_i, 2, '0')), v_dish_desc = '轻食净菜，开袋即用', v_dish_price = ROUND(10 + RAND() * 10, 2), v_flavor_name1 = '规格', v_flavor_value1 = '["单人份","双人份"]', v_flavor_name2 = NULL, v_flavor_value2 = NULL;
                        WHEN 4 THEN SET v_dish_name = CONCAT('圣女果', LPAD(v_i, 2, '0')), v_dish_desc = '酸甜均衡，适合即食', v_dish_price = ROUND(7 + RAND() * 8, 2), v_flavor_name1 = '规格', v_flavor_value1 = '["250g","500g"]', v_flavor_name2 = NULL, v_flavor_value2 = NULL;
                        ELSE SET v_dish_name = CONCAT('西兰花', LPAD(v_i, 2, '0')), v_dish_desc = '新鲜采摘，适合蒸煮快炒', v_dish_price = ROUND(6 + RAND() * 7, 2), v_flavor_name1 = '规格', v_flavor_value1 = '["500g","1000g"]', v_flavor_name2 = NULL, v_flavor_value2 = NULL;
                    END CASE;
            END CASE;

            INSERT INTO dish (
                merchant_id, category_id, name, price, image, description, status,
                create_time, update_time, create_user, update_user
            ) VALUES (
                v_merchant_id, v_target_category_id, v_dish_name, v_dish_price, v_dish_image, v_dish_desc, 1,
                v_now, v_now, v_operator_id, v_operator_id
            );

            SET v_dish_id = LAST_INSERT_ID();

            IF v_flavor_name1 IS NOT NULL THEN
                INSERT INTO dish_flavor (dish_id, name, value)
                VALUES (v_dish_id, v_flavor_name1, v_flavor_value1);
            END IF;

            IF v_flavor_name2 IS NOT NULL THEN
                INSERT INTO dish_flavor (dish_id, name, value)
                VALUES (v_dish_id, v_flavor_name2, v_flavor_value2);
            END IF;

            SET v_i = v_i + 1;
        END WHILE;

        SET v_j = 1;
        WHILE v_j <= p_setmeal_per_merchant DO
            CASE v_biz_category_id
                WHEN 1 THEN
                    SET v_setmeal_name = CONCAT('工作日双拼套餐', LPAD(v_j, 2, '0'));
                    SET v_setmeal_desc = '主食搭配小吃饮品，适合午晚餐';
                WHEN 2 THEN
                    SET v_setmeal_name = CONCAT('双人下午茶套餐', LPAD(v_j, 2, '0'));
                    SET v_setmeal_desc = '饮品搭配甜点，适合社交分享';
                WHEN 3 THEN
                    SET v_setmeal_name = CONCAT('宅家囤货组合', LPAD(v_j, 2, '0'));
                    SET v_setmeal_desc = '高频消耗品打包更划算';
                ELSE
                    SET v_setmeal_name = CONCAT('家庭果蔬组合', LPAD(v_j, 2, '0'));
                    SET v_setmeal_desc = '适合两到三天家庭食材采购';
            END CASE;

            SELECT ROUND(SUM(price), 2) INTO v_price_sum
            FROM (
                SELECT price
                FROM dish
                WHERE merchant_id = v_merchant_id
                ORDER BY RAND()
                LIMIT 3
            ) t;

            SET v_setmeal_price = ROUND(v_price_sum * (0.85 + RAND() * 0.10), 2);

            INSERT INTO setmeal (
                merchant_id, category_id, name, price, image, description, status,
                create_time, update_time, create_user, update_user
            ) VALUES (
                v_merchant_id, v_setmeal_category_id, v_setmeal_name, v_setmeal_price, v_setmeal_image, v_setmeal_desc, 1,
                v_now, v_now, v_operator_id, v_operator_id
            );

            SET v_setmeal_id = LAST_INSERT_ID();

            SET v_pick = 1;
            WHILE v_pick <= 3 DO
                INSERT INTO setmeal_dish (setmeal_id, dish_id, name, price, copies)
                SELECT v_setmeal_id, d.id, d.name, d.price, 1 + FLOOR(RAND() * 2)
                FROM dish d
                WHERE d.merchant_id = v_merchant_id
                  AND d.id NOT IN (
                      SELECT sd.dish_id
                      FROM setmeal_dish sd
                      WHERE sd.setmeal_id = v_setmeal_id
                  )
                ORDER BY RAND()
                LIMIT 1;

                SET v_pick = v_pick + 1;
            END WHILE;

            SET v_j = v_j + 1;
        END WHILE;
    END LOOP;

    CLOSE cur_merchant;

    COMMIT;
END$$

CREATE PROCEDURE sp_clear_menu_by_batch(
    IN p_batch_no VARCHAR(32)
)
BEGIN
    DECLARE v_batch_code VARCHAR(32);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF p_batch_no IS NULL OR TRIM(p_batch_no) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '批次号不能为空';
    END IF;

    SET v_batch_code = UPPER(REPLACE(TRIM(p_batch_no), '-', ''));

    START TRANSACTION;

    DELETE sd
    FROM setmeal_dish sd
    JOIN setmeal s ON s.id = sd.setmeal_id
    JOIN merchant m ON m.id = s.merchant_id
    WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(v_batch_code))
          = CAST(v_batch_code AS BINARY);

    DELETE s
    FROM setmeal s
    JOIN merchant m ON m.id = s.merchant_id
    WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(v_batch_code))
          = CAST(v_batch_code AS BINARY);

    DELETE df
    FROM dish_flavor df
    JOIN dish d ON d.id = df.dish_id
    JOIN merchant m ON m.id = d.merchant_id
    WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(v_batch_code))
          = CAST(v_batch_code AS BINARY);

    DELETE d
    FROM dish d
    JOIN merchant m ON m.id = d.merchant_id
    WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(v_batch_code))
          = CAST(v_batch_code AS BINARY);

    DELETE c
    FROM category c
    JOIN merchant m ON m.id = c.merchant_id
    WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(v_batch_code))
          = CAST(v_batch_code AS BINARY);

    COMMIT;
END$$

DELIMITER ;

-- =========================
-- 示例执行顺序
-- =========================
-- 1. 生成分类
-- CALL sp_mock_categories_by_batch('T250425A', 1);
--
-- 2. 生成菜单
-- CALL sp_mock_menu_by_batch('T250425A', 1, 12, 3);
--
-- 3. 如需清理菜单（不删除商户和账号）
-- CALL sp_clear_menu_by_batch('T250425A');
--
-- =========================
-- 校验 SQL
-- =========================
-- SET @batch_no = 'T250425A';
--
-- 1. 每商户分类数
-- SELECT m.id AS merchant_id, m.merchant_name,
--        SUM(CASE WHEN c.type = 1 THEN 1 ELSE 0 END) AS dish_category_count,
--        SUM(CASE WHEN c.type = 2 THEN 1 ELSE 0 END) AS setmeal_category_count,
--        COUNT(*) AS total_category_count
-- FROM merchant m
-- JOIN category c ON c.merchant_id = m.id
-- WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(UPPER(REPLACE(@batch_no, '-', ''))))
--       = CAST(UPPER(REPLACE(@batch_no, '-', '')) AS BINARY)
-- GROUP BY m.id, m.merchant_name
-- ORDER BY m.id;
--
-- 2. 每商户菜品数、口味数
-- SELECT m.id AS merchant_id, m.merchant_name,
--        COUNT(DISTINCT d.id) AS dish_count,
--        COUNT(df.id) AS flavor_count
-- FROM merchant m
-- LEFT JOIN dish d ON d.merchant_id = m.id
-- LEFT JOIN dish_flavor df ON df.dish_id = d.id
-- WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(UPPER(REPLACE(@batch_no, '-', ''))))
--       = CAST(UPPER(REPLACE(@batch_no, '-', '')) AS BINARY)
-- GROUP BY m.id, m.merchant_name
-- ORDER BY m.id;
--
-- 3. 每商户套餐数、套餐明细数
-- SELECT m.id AS merchant_id, m.merchant_name,
--        COUNT(DISTINCT s.id) AS setmeal_count,
--        COUNT(sd.id) AS setmeal_dish_count
-- FROM merchant m
-- LEFT JOIN setmeal s ON s.merchant_id = m.id
-- LEFT JOIN setmeal_dish sd ON sd.setmeal_id = s.id
-- WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(UPPER(REPLACE(@batch_no, '-', ''))))
--       = CAST(UPPER(REPLACE(@batch_no, '-', '')) AS BINARY)
-- GROUP BY m.id, m.merchant_name
-- ORDER BY m.id;
--
-- 4. 分类维度概览
-- SELECT c.type, c.name,
--        COUNT(DISTINCT c.id) AS category_count,
--        COUNT(DISTINCT d.id) AS dish_count,
--        COUNT(DISTINCT s.id) AS setmeal_count
-- FROM merchant m
-- JOIN category c ON c.merchant_id = m.id
-- LEFT JOIN dish d ON d.category_id = c.id
-- LEFT JOIN setmeal s ON s.category_id = c.id
-- WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(UPPER(REPLACE(@batch_no, '-', ''))))
--       = CAST(UPPER(REPLACE(@batch_no, '-', '')) AS BINARY)
-- GROUP BY c.type, c.name
-- ORDER BY c.type, c.name;
