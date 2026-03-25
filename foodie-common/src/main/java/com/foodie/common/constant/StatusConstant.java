package com.foodie.common.constant;

/**
 * 状态常量，启用或者禁用
 */
public class StatusConstant {

   /* //启用
    public static final Integer ENABLE = 1;

    //禁用
    public static final Integer DISABLE = 0;*/



        // 账号状态
        public static final Integer ENABLE = 1;
        public static final Integer DISABLE = 0;

        // 商户审核状态
        public static final Integer AUDIT_PASSED = 1;
        public static final Integer AUDIT_PENDING = 2;
        public static final Integer AUDIT_REJECTED = 3;

        // 商户营业状态
        public static final Integer MERCHANT_OPEN = 1;      // 营业中
        public static final Integer MERCHANT_REST = 2;      // 休息中
        public static final Integer MERCHANT_CLOSED = 3;    // 已关闭
        public static final Integer MERCHANT_PENDING = 0;   // 待审核

        // 分类类型
        public static final Integer CATEGORY_TYPE_DISH = 1;     // 菜品分类
        public static final Integer CATEGORY_TYPE_SETMEAL = 2;  // 套餐分类

        // 菜品状态
        public static final Integer DISH_ON_SALE = 1;    // 起售
        public static final Integer DISH_STOP_SALE = 0;  // 停售

        // 套餐状态
        public static final Integer SETMEAL_ON_SALE = 1;    // 起售
        public static final Integer SETMEAL_STOP_SALE = 0;  // 停售

        // 订单状态
        public static final Integer ORDER_PENDING_PAYMENT = 1;  // 待付款
        public static final Integer ORDER_TO_BE_CONFIRMED = 2;  // 待接单
        public static final Integer ORDER_CONFIRMED = 3;        // 已接单
        public static final Integer ORDER_DELIVERY_IN_PROGRESS = 4; // 派送中
        public static final Integer ORDER_COMPLETED = 5;        // 已完成
        public static final Integer ORDER_CANCELLED = 6;        // 已取消
        public static final Integer ORDER_REFUNDED = 7;         // 已退款

        // 支付状态
        public static final Integer PAY_STATUS_UNPAID = 0;      // 未支付
        public static final Integer PAY_STATUS_PAID = 1;        // 已支付
        public static final Integer PAY_STATUS_REFUNDING = 2;   // 退款中
        public static final Integer PAY_STATUS_REFUNDED = 3;    // 已退款

        // 支付方式
        public static final Integer PAY_METHOD_WECHAT = 1;      // 微信支付
        public static final Integer PAY_METHOD_ALIPAY = 2;      // 支付宝支付

        //结算状态
        public static final Integer STATUS_PENDING = 1;    // 待结算
        public static final Integer STATUS_SETTLED = 2;    // 已结算
        public static final Integer STATUS_CANCELLED = 3;  // 已取消


        /**
         * 配置类型常量
         */
        public static final Integer TYPE_GLOBAL = 1;      // 全局默认
        public static final Integer TYPE_MERCHANT = 2;    // 商户特定
        public static final Integer TYPE_CATEGORY = 3;    //

}
