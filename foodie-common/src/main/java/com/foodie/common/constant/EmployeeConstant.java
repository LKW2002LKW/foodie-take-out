package com.foodie.common.constant;

/**
 * 员工相关常量
 */
public class EmployeeConstant {

    /**
     * 角色
     */
    public static final int ROLE_ADMIN = 1;    // 管理员
    public static final int ROLE_MANAGER = 2;  // 经理
    public static final int ROLE_EMPLOYEE = 3; // 普通员工

    /**
     * 状态
     */
    public static final int STATUS_NORMAL = 1;   // 正常
    public static final int STATUS_DISABLED = 0; // 禁用

    /**
     * 默认密码
     */
    public static final String DEFAULT_PASSWORD = "123456";

    /**
     * 获取角色名称
     */
    public static String getRoleName(Integer role) {
        if (role == null) {
            return "未知";
        }
        switch (role) {
            case ROLE_ADMIN:
                return "管理员";
            case ROLE_MANAGER:
                return "经理";
            case ROLE_EMPLOYEE:
                return "普通员工";
            default:
                return "未知";
        }
    }

    /**
     * 获取状态名称
     */
    public static String getStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        return status == STATUS_NORMAL ? "正常" : "禁用";
    }

    /**
     * 身份证号脱敏
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(14);
    }
}