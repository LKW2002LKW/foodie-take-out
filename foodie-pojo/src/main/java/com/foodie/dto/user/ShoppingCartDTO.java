package com.foodie.dto.user;

import lombok.Data;
import java.io.Serializable;

@Data
public class ShoppingCartDTO implements Serializable {

    private Long dishId;        // 菜品ID（菜品和套餐二选一）
    private Long setmealId;     // 套餐ID（菜品和套餐二选一）
    private Long merchantId;    //商户id
    private String dishFlavor;  // 菜品口味（JSON格式）
    private Integer number;     // 数量（加购时传1，修改时传具体数量）
}