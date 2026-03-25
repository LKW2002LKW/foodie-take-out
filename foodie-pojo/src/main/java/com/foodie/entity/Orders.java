package com.foodie.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单表
 */
@Data
@TableName("orders")
public class Orders implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 订单号 */
    private String orderNumber;

    /** 用户ID */
    private Long userId;

    /** 商户ID */
    private Long merchantId;

    /** 地址ID */
    private Long addressBookId;

    /**
     * 订单状态
     * 1待付款 2待接单 3已接单 4配送中 5已完成 6已取消 7已退款
     */
    private Integer status;

    /** 下单时间 */
    private LocalDateTime orderTime;

    /** 结账时间 */
    private LocalDateTime checkoutTime;

    /**
     * 支付方式
     * 1微信支付 2支付宝
     */
    private Integer payMethod;

    /**
     * 支付状态
     * 0未支付 1已支付 2退款中 3已退款
     */
    private Integer payStatus;

    /** 商品总额 */
    private BigDecimal amount;

    /** 配送费 */
    private BigDecimal deliveryFee;

    /** 打包费 */
    private BigDecimal packAmount;

    /** 平台抽成 */
    private BigDecimal platformCommission;

    /** 商户实收金额 */
    private BigDecimal merchantAmount;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 备注信息 */
    private String remark;

    /** 收货电话 */
    private String phone;

    /** 收货地址 */
    private String address;

    /** 收货人 */
    private String consignee;

    /** 下单用户名 */
    private String userName;

    /** 取消原因 */
    private String cancelReason;

    /** 拒单原因 */
    private String rejectionReason;

    /** 取消时间 */
    private LocalDateTime cancelTime;

    /** 预计送达时间 */
    private LocalDateTime estimatedDeliveryTime;

    /** 实际送达时间 */
    private LocalDateTime deliveryTime;

    /** 餐具数量 */
    private Integer tablewareNumber;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}