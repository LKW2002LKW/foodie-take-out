package com.foodie.common.constant;

/**
 * Redis keys and prefixes used across the application.
 * This is a lightweight skeleton file created for project structure.
 */
public class RedisKeyConstant {

    // 购物车缓存：cart:user:{userId}:merchant:{merchantId}
    public static final String SHOPPING_CART = "cart:user:%s:merchant:%s";

    // 验证码缓存：sms:code:{phone}
    public static final String SMS_CODE = "sms:code:%s";

    // Token缓存：token:{userType}:{userId}
    public static final String TOKEN = "token:%s:%s";

    // 商户营业状态：merchant:status:{merchantId}
    public static final String MERCHANT_STATUS = "merchant:status:%s";

}

