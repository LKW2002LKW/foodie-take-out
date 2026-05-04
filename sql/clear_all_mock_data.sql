-- 总控清理脚本
SET @merchant_batch = 'T250425A';
SET @user_batch = 'U250425A';

-- 1. 平台抽成配置、多周期结算、单周期结算
CALL sp_clear_platform_commission_mock(@merchant_batch);
CALL sp_clear_multi_cycle_settlement_by_batch(@merchant_batch, '2026-01', '2026-04');
CALL sp_clear_settlement_by_batch(@merchant_batch, '2026-04');

-- 2. 购物车
CALL sp_clear_cart_by_batch(@user_batch);

-- 3. 用户、订单、评价
CALL sp_clear_user_order_data_by_batch(@merchant_batch, @user_batch);

-- 4. 菜单
CALL sp_clear_menu_by_batch(@merchant_batch);

-- 5. 如需清理商户和商户管理员，取消下面注释
-- START TRANSACTION;
--
-- DELETE ma
-- FROM merchant_admin ma
-- JOIN merchant m ON m.id = ma.merchant_id
-- WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(UPPER(REPLACE(@merchant_batch, '-', ''))))
--       = CAST(UPPER(REPLACE(@merchant_batch, '-', '')) AS BINARY);
--
-- DELETE FROM merchant
-- WHERE LEFT(CAST(merchant_code AS BINARY), CHAR_LENGTH(UPPER(REPLACE(@merchant_batch, '-', ''))))
--       = CAST(UPPER(REPLACE(@merchant_batch, '-', '')) AS BINARY);
--
-- COMMIT;
