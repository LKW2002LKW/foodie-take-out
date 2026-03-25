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
 * 购物车表
 */
@Data
@TableName("shopping_cart")
public class ShoppingCart implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 商户ID */
    private Long merchantId;

    /** 菜品ID */
    private Long dishId;

    /** 套餐ID */
    private Long setmealId;

    /** 商品名称 */
    private String name;

    /** 商品图片 */
    private String image;

    /** 菜品口味 */
    private String dishFlavor;

    /** 商品数量 */
    private Integer number;

    /** 商品单价 */
    private BigDecimal amount;

    /** 创建时间 */
    private LocalDateTime createTime;
}