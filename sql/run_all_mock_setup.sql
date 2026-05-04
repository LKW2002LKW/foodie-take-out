-- 总控初始化脚本
-- 前提：以下过程定义文件已提前 SOURCE 导入
-- 1. mock_merchants_nearby.sql
-- 2. mock_menu_by_batch.sql
-- 3. mock_user_order_by_batch.sql
-- 4. mock_cart_settlement_by_batch.sql
-- 5. mock_finance_config_by_batch.sql

-- 参数约定
SET @merchant_batch = 'T250425A';
SET @user_batch = 'U250425A';
SET @center_lng = 119.943466;
SET @center_lat = 32.464328;

-- 1. 商户和商户管理员
CALL sp_mock_merchants_nearby(@center_lng, @center_lat, 5.00, 100, @merchant_batch, 1);

-- 2. 店内分类、菜品、套餐
CALL sp_mock_categories_by_batch(@merchant_batch, 1);
CALL sp_mock_menu_by_batch(@merchant_batch, 1, 12, 3);

-- 3. 用户、地址、订单、评价
CALL sp_mock_users_and_addresses(@user_batch, 40, 2, @center_lng, @center_lat);
CALL sp_mock_orders_by_batch(@merchant_batch, @user_batch, 600);
CALL sp_mock_reviews_by_batch(@merchant_batch, @user_batch, 0.60);

-- 4. 购物车、单周期结算
CALL sp_mock_cart_by_batch(@merchant_batch, @user_batch, 20, 4);
CALL sp_mock_settlement_by_batch(@merchant_batch, '2026-04', 1);

-- 5. 多周期结算、平台抽成配置
CALL sp_mock_multi_cycle_settlement_by_batch(@merchant_batch, '2026-04', 4, 1);
CALL sp_mock_platform_commission_config(@merchant_batch, 1);
