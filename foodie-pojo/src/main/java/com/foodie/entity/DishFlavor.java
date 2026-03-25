package com.foodie.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;

@Data
@TableName("dish_flavor")
public class DishFlavor implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long dishId;    // 菜品ID
    private String name;    // 口味名称（如：辣度、甜度）
    private String value;   // 口味值列表（JSON格式）
}