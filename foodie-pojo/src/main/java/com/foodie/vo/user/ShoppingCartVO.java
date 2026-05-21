package com.foodie.vo.user;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ShoppingCartVO implements Serializable {

    private Long id;
    private Long userId;
    private Long merchantId;
    private String merchantName;   // 商户名称（冗余）
    private Long dishId;
    private Long setmealId;
    private Long categoryId;
    private String name;           // 商品名称
    private String image;          // 商品图片
    private String dishFlavor;     // 菜品口味
    private Integer number;        // 数量
    private BigDecimal amount;     // 单价
    private LocalDateTime createTime;
}
