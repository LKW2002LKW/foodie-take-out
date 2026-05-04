# Mock Data Runbook

## 批次约定

- 商户批次：`T250425A`
- 用户批次：`U250425A`
- 结算结束周期：`2026-04`
- 多周期结算范围：`2026-01 ~ 2026-04`

建议规范：

- 商户批次统一用 `T` 开头，例如 `T250425A`
- 用户批次统一用 `U` 开头，例如 `U250425A`
- 同一次环境初始化，商户批次和用户批次固定，不要混用
- 重跑前优先执行对应清理 SQL，避免重复批次冲突

## 文件说明

- `mock_merchants_nearby.sql`：商户、商户管理员
- `mock_menu_by_batch.sql`：店内分类、菜品、口味、套餐、套餐明细
- `mock_user_order_by_batch.sql`：用户、地址、订单、订单明细、评价
- `mock_cart_settlement_by_batch.sql`：购物车、单周期结算
- `mock_finance_config_by_batch.sql`：多周期结算、平台抽成配置
- `run_all_mock_setup.sql`：总控初始化调用脚本
- `clear_all_mock_data.sql`：总控清理脚本

## 一次性导入存储过程定义

先在 MySQL 客户端执行以下文件，确保所有存储过程已创建：

```sql
SOURCE /mnt/d/baidunetdiskdownload/黑马/苍穹/项目/foodie-take-out/mock_merchants_nearby.sql;
SOURCE /mnt/d/baidunetdiskdownload/黑马/苍穹/项目/foodie-take-out/mock_menu_by_batch.sql;
SOURCE /mnt/d/baidunetdiskdownload/黑马/苍穹/项目/foodie-take-out/mock_user_order_by_batch.sql;
SOURCE /mnt/d/baidunetdiskdownload/黑马/苍穹/项目/foodie-take-out/mock_cart_settlement_by_batch.sql;
SOURCE /mnt/d/baidunetdiskdownload/黑马/苍穹/项目/foodie-take-out/mock_finance_config_by_batch.sql;
```

## 推荐初始化顺序

### 方式一：直接执行总控脚本

```sql
SOURCE /mnt/d/baidunetdiskdownload/黑马/苍穹/项目/foodie-take-out/run_all_mock_setup.sql;
```

### 方式二：手工逐步执行

```sql
CALL sp_mock_merchants_nearby(119.943466, 32.464328, 5.00, 100, 'T250425A', 1);
CALL sp_mock_categories_by_batch('T250425A', 1);
CALL sp_mock_menu_by_batch('T250425A', 1, 12, 3);

CALL sp_mock_users_and_addresses('U250425A', 40, 2, 119.943466, 32.464328);
CALL sp_mock_orders_by_batch('T250425A', 'U250425A', 600);
CALL sp_mock_reviews_by_batch('T250425A', 'U250425A', 0.60);

CALL sp_mock_cart_by_batch('T250425A', 'U250425A', 20, 4);
CALL sp_mock_settlement_by_batch('T250425A', '2026-04', 1);
CALL sp_mock_multi_cycle_settlement_by_batch('T250425A', '2026-04', 4, 1);
CALL sp_mock_platform_commission_config('T250425A', 1);
```

## 推荐清理顺序

### 方式一：直接执行总清理脚本

```sql
SOURCE /mnt/d/baidunetdiskdownload/黑马/苍穹/项目/foodie-take-out/clear_all_mock_data.sql;
```

### 方式二：手工逐步执行

```sql
CALL sp_clear_platform_commission_mock('T250425A');
CALL sp_clear_multi_cycle_settlement_by_batch('T250425A', '2026-01', '2026-04');
CALL sp_clear_settlement_by_batch('T250425A', '2026-04');
CALL sp_clear_cart_by_batch('U250425A');
CALL sp_clear_user_order_data_by_batch('T250425A', 'U250425A');
CALL sp_clear_menu_by_batch('T250425A');
```

如需连商户和商户管理员一起清理，执行：

```sql
SET @batch_no = 'T250425A';

START TRANSACTION;

DELETE ma
FROM merchant_admin ma
JOIN merchant m ON m.id = ma.merchant_id
WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(UPPER(REPLACE(@batch_no, '-', ''))))
      = CAST(UPPER(REPLACE(@batch_no, '-', '')) AS BINARY);

DELETE FROM merchant
WHERE LEFT(CAST(merchant_code AS BINARY), CHAR_LENGTH(UPPER(REPLACE(@batch_no, '-', ''))))
      = CAST(UPPER(REPLACE(@batch_no, '-', '')) AS BINARY);

COMMIT;
```

## 快速验收

### 1. 商户

```sql
SET @merchant_batch = 'T250425A';

SELECT COUNT(*) AS merchant_count
FROM merchant
WHERE LEFT(CAST(merchant_code AS BINARY), CHAR_LENGTH(UPPER(REPLACE(@merchant_batch, '-', ''))))
      = CAST(UPPER(REPLACE(@merchant_batch, '-', '')) AS BINARY);
```

预期：`100`

### 2. 菜单

```sql
SELECT COUNT(*) AS category_count
FROM category c
JOIN merchant m ON m.id = c.merchant_id
WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(UPPER(REPLACE(@merchant_batch, '-', ''))))
      = CAST(UPPER(REPLACE(@merchant_batch, '-', '')) AS BINARY);

SELECT COUNT(*) AS dish_count
FROM dish d
JOIN merchant m ON m.id = d.merchant_id
WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(UPPER(REPLACE(@merchant_batch, '-', ''))))
      = CAST(UPPER(REPLACE(@merchant_batch, '-', '')) AS BINARY);

SELECT COUNT(*) AS setmeal_count
FROM setmeal s
JOIN merchant m ON m.id = s.merchant_id
WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(UPPER(REPLACE(@merchant_batch, '-', ''))))
      = CAST(UPPER(REPLACE(@merchant_batch, '-', '')) AS BINARY);
```

预期：

- `category_count = 400`
- `dish_count = 1200`
- `setmeal_count = 300`

### 3. 用户、订单、评价

```sql
SET @user_batch = 'U250425A';

SELECT COUNT(*) AS user_count
FROM `user`
WHERE LEFT(CAST(openid AS BINARY), CHAR_LENGTH(CONCAT('UB', UPPER(REPLACE(@user_batch, '-', '')))))
      = CAST(CONCAT('UB', UPPER(REPLACE(@user_batch, '-', ''))) AS BINARY);

SELECT COUNT(*) AS order_count
FROM orders
WHERE LEFT(CAST(order_number AS BINARY), CHAR_LENGTH(CONCAT('OB', LEFT(UPPER(REPLACE(@merchant_batch, '-', '')), 8), LEFT(UPPER(REPLACE(@user_batch, '-', '')), 8))))
      = CAST(CONCAT('OB', LEFT(UPPER(REPLACE(@merchant_batch, '-', '')), 8), LEFT(UPPER(REPLACE(@user_batch, '-', '')), 8)) AS BINARY);

SELECT COUNT(*) AS review_count
FROM order_review r
JOIN orders o ON o.id = r.order_id
WHERE LEFT(CAST(o.order_number AS BINARY), CHAR_LENGTH(CONCAT('OB', LEFT(UPPER(REPLACE(@merchant_batch, '-', '')), 8), LEFT(UPPER(REPLACE(@user_batch, '-', '')), 8))))
      = CAST(CONCAT('OB', LEFT(UPPER(REPLACE(@merchant_batch, '-', '')), 8), LEFT(UPPER(REPLACE(@user_batch, '-', '')), 8)) AS BINARY);
```

推荐预期：

- `user_count = 40`
- `order_count = 600`
- `review_count` 接近完成单的 60%

### 4. 购物车、结算、抽成配置

```sql
SELECT COUNT(*) AS cart_row_count
FROM shopping_cart sc
JOIN `user` u ON u.id = sc.user_id
WHERE LEFT(CAST(u.openid AS BINARY), CHAR_LENGTH(CONCAT('UB', UPPER(REPLACE(@user_batch, '-', '')))))
      = CAST(CONCAT('UB', UPPER(REPLACE(@user_batch, '-', ''))) AS BINARY);

SELECT COUNT(*) AS settlement_count
FROM merchant_settlement ms
JOIN merchant m ON m.id = ms.merchant_id
WHERE LEFT(CAST(m.merchant_code AS BINARY), CHAR_LENGTH(UPPER(REPLACE(@merchant_batch, '-', ''))))
      = CAST(UPPER(REPLACE(@merchant_batch, '-', '')) AS BINARY);

SELECT config_type, COUNT(*) AS cnt
FROM platform_commission
GROUP BY config_type
ORDER BY config_type;
```

## 页面联调建议

### 用户端

- 首页商户列表：检查销量、评分、距离
- 商户详情：检查分类、菜品、套餐、评价
- 购物车：检查商品、规格、套餐混合展示
- 我的订单：检查各状态订单
- 我的评价：检查评价记录和带图数据

### 商户端

- 订单列表：检查多状态订单
- 评价管理：检查评分、内容、图片、回复
- 财务页：检查收入明细、结算记录
- 统计页：检查订单数、营收、评分

### 平台端

- 商户管理：检查商户详情和订单量
- 财务结算：检查多周期结算数据
- 抽成配置：检查全局、分类、商户级配置
- 财务报表：检查总收入、平台抽成、待结算金额
