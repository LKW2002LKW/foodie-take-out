package com.foodie.common.constant;

/**
 * 信息提示常量类
 */
public class MessageConstant {


   public static final String PASSWORD_ERROR = "密码错误";
        public static final String ACCOUNT_NOT_FOUND = "账号不存在";
        public static final String ACCOUNT_LOCKED = "账号已被锁定";
        public static final String UNKNOWN_ERROR = "未知错误";
        public static final String USER_NOT_LOGIN = "用户未登录";
        public static final String LOGIN_FAILED = "登录失败";
        public static final String MERCHANT_NOT_FOUND = "商户不存在";
        public static final String MERCHANT_NOT_APPROVED = "商户未通过审核";
        public static final String PHONE_ALREADY_EXISTS = "手机号已被注册";
        public static final String USERNAME_ALREADY_EXISTS = "用户名已被占用";
        public static final String MERCHANT_NAME_ALREADY_EXISTS = "商户名称已存在";

        // JWT相关
        public static final String TOKEN_INVALID = "Token无效";
        public static final String TOKEN_EXPIRED = "Token已过期";
        public static final String NO_PERMISSION = "无权限访问";

        // 商户信息管理相关
        public static final String MERCHANT_INFO_UPDATE_SUCCESS = "商户信息修改成功";
        public static final String MERCHANT_STATUS_UPDATE_SUCCESS = "营业状态修改成功";
        public static final String MERCHANT_BUSINESS_HOURS_UPDATE_SUCCESS = "营业时间修改成功";
        public static final String MERCHANT_AUDIT_PENDING = "商户正在审核中，暂时无法修改信息";
        public static final String FILE_UPLOAD_FAILED = "文件上传失败";
        public static final String FILE_TYPE_ERROR = "文件格式不正确";

     // 分类管理相关
     public static final String CATEGORY_NAME_ALREADY_EXISTS = "分类名称已存在";
     public static final String CATEGORY_NOT_FOUND = "分类不存在";
     public static final String CATEGORY_BE_RELATED_BY_DISH = "当前分类关联了菜品，不能删除";
     public static final String CATEGORY_BE_RELATED_BY_SETMEAL = "当前分类关联了套餐，不能删除";
     public static final String CATEGORY_ADD_SUCCESS = "分类添加成功";
     public static final String CATEGORY_UPDATE_SUCCESS = "分类修改成功";
     public static final String CATEGORY_DELETE_SUCCESS = "分类删除成功";
     public static final String CATEGORY_STATUS_UPDATE_SUCCESS = "分类状态修改成功";

    //菜品管理相关
    public static final String DISH_NAME_ALREADY_EXISTS = "菜品名称已存在";
    public static final String DISH_NOT_FOUND = "菜品不存在";
    public static final String DISH_ADD_SUCCESS = "菜品添加成功";
    public static final String DISH_UPDATE_SUCCESS = "菜品修改成功";
    public static final String DISH_DELETE_SUCCESS = "菜品删除成功";
    public static final String DISH_STATUS_UPDATE_SUCCESS = "菜品状态修改成功";
    public static final String DISH_ON_SALE = "起售中的菜品不能删除";
    public static final String DISH_BE_RELATED_BY_SETMEAL = "当前菜品关联了套餐，不能删除";

    // 套餐管理相关
    public static final String SETMEAL_NAME_ALREADY_EXISTS = "套餐名称已存在";
    public static final String SETMEAL_NOT_FOUND = "套餐不存在";
    public static final String SETMEAL_ADD_SUCCESS = "套餐添加成功";
    public static final String SETMEAL_UPDATE_SUCCESS = "套餐修改成功";
    public static final String SETMEAL_DELETE_SUCCESS = "套餐删除成功";
    public static final String SETMEAL_STATUS_UPDATE_SUCCESS = "套餐状态修改成功";
    public static final String SETMEAL_ON_SALE = "起售中的套餐不能删除";
    public static final String SETMEAL_ENABLE_FAILED = "套餐内包含停售菜品，无法起售";
    public static final String SETMEAL_DISH_NOT_NULL = "套餐必须包含菜品";

    // 用户认证相关
    public static final String USER_REGISTER_SUCCESS = "注册成功";
    public static final String USER_LOGIN_SUCCESS = "登录成功";
    public static final String PHONE_ALREADY_REGISTERED = "手机号已被注册";
    public static final String CODE_SEND_SUCCESS = "验证码发送成功";
    public static final String CODE_ERROR = "验证码错误";
    public static final String CODE_EXPIRED = "验证码已过期";
    public static final String PHONE_NOT_REGISTERED = "手机号未注册";
    public static final String WECHAT_LOGIN_FAILED = "微信登录失败";

    // 地址管理相关
    public static final String ADDRESS_ADD_SUCCESS = "地址添加成功";
    public static final String ADDRESS_UPDATE_SUCCESS = "地址修改成功";
    public static final String ADDRESS_DELETE_SUCCESS = "地址删除成功";
    public static final String ADDRESS_NOT_FOUND = "地址不存在";
    public static final String ADDRESS_SET_DEFAULT_SUCCESS = "默认地址设置成功";
    public static final String ADDRESS_LOCATION_REQUIRED = "请先选择地图候选地址";
    //购物车管理
    public static final String CART_ADD_SUCCESS = "加入购物车成功";
    public static final String CART_UPDATE_SUCCESS = "购物车更新成功";
    public static final String CART_CLEAR_SUCCESS = "购物车已清空";
    public static final String CART_ITEM_NOT_FOUND = "购物车商品不存在";
    public static final String DIFFERENT_MERCHANT = "购物车中已有其他商户的商品，请先清空购物车";
    public static final String DISH_NOT_ON_SALE = "菜品已停售";
    public static final String SETMEAL_NOT_ON_SALE = "套餐已停售";

    // 订单相关
    public static final String ORDER_SUBMIT_SUCCESS = "订单提交成功";
    public static final String ORDER_NOT_FOUND = "订单不存在";
    public static final String ORDER_STATUS_ERROR = "订单状态错误";
    public static final String ADDRESS_EMPTY = "请选择收货地址";
    public static final String CART_EMPTY = "购物车为空，无法下单";
    public static final String MERCHANT_NOT_OPEN = "商户未营业";
    public static final String AMOUNT_NOT_ENOUGH = "订单金额未达到起送金额";
    public static final String PAYMENT_SUCCESS = "支付成功";
    public static final String PAYMENT_FAILED = "支付失败";



    public static final String ORDER_NOT_BELONG_TO_MERCHANT = "该订单不属于当前商户";



    // 登录相关
    public static final String LOGIN_SUCCESS = "登录成功";

    // 商户相关
    public static final String MERCHANT_AUDIT_SUCCESS = "审核成功";
    public static final String MERCHANT_STATUS_ERROR = "商户状态错误";

    // 通用
    public static final String SUCCESS = "操作成功";


}
