package com.foodie.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@TableName("setmeal_dish")
public class SetmealDish implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long setmealId;    // 套餐ID
    private Long dishId;       // 菜品ID
    private String name;       // 菜品名称（冗余）
    private BigDecimal price;  // 菜品单价（冗余）
    private Integer copies;    // 菜品份数
}