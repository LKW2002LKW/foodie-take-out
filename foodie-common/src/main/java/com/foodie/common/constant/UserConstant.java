package com.foodie.common.constant;

/**
 * 用户相关常量
 */
public class UserConstant {

    /**
     * 性别
     */
    public static final int SEX_UNKNOWN = 0; // 未知
    public static final int SEX_MALE = 1;    // 男
    public static final int SEX_FEMALE = 2;  // 女

    /**
     * 状态
     */
    public static final int STATUS_NORMAL = 1;  // 正常
    public static final int STATUS_DISABLED = 0; // 禁用

    /**
     * 验证码前缀
     */
    public static final String SMS_CODE_PREFIX = "sms:code:";

    /**
     * 验证码有效期（分钟）
     */
    public static final int SMS_CODE_EXPIRE_MINUTES = 5;

    /**
     * 获取性别名称
     */
    public static String getSexName(Integer sex) {
        if (sex == null) {
            return "未知";
        }
        switch (sex) {
            case SEX_MALE:
                return "男";
            case SEX_FEMALE:
                return "女";
            default:
                return "未知";
        }
    }
}