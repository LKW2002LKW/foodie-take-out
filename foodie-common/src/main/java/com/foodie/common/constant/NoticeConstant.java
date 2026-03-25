package com.foodie.common.constant;

/**
 * 公告相关常量
 */
public class NoticeConstant {

    /**
     * 公告类型
     */
    public static final int TYPE_SYSTEM = 1;    // 系统公告
    public static final int TYPE_ACTIVITY = 2;  // 活动公告

    /**
     * 目标对象
     */
    public static final int TARGET_MERCHANT = 1; // 商户
    public static final int TARGET_USER = 2;     // 用户
    public static final int TARGET_ALL = 3;      // 全部

    /**
     * 状态
     */
    public static final int STATUS_DRAFT = 0;     // 草稿
    public static final int STATUS_PUBLISHED = 1; // 已发布

    /**
     * 获取类型名称
     */
    public static String getTypeName(Integer type) {
        if (type == null) {
            return "未知";
        }
        switch (type) {
            case TYPE_SYSTEM:
                return "系统公告";
            case TYPE_ACTIVITY:
                return "活动公告";
            default:
                return "未知";
        }
    }

    /**
     * 获取目标对象名称
     */
    public static String getTargetName(Integer target) {
        if (target == null) {
            return "未知";
        }
        switch (target) {
            case TARGET_MERCHANT:
                return "商户";
            case TARGET_USER:
                return "用户";
            case TARGET_ALL:
                return "全部";
            default:
                return "未知";
        }
    }
}