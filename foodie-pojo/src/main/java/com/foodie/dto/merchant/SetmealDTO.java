package com.foodie.dto.merchant;

import com.foodie.entity.SetmealDish;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SetmealDTO implements Serializable {

    private Long id;
    private String name;              // 套餐名称
    private Long categoryId;          // 分类ID
    private BigDecimal price;         // 套餐价格
    private String image;             // 图片路径
    private String description;       // 套餐描述
    private List<SetmealDish> setmealDishes; // 套餐菜品列表
}