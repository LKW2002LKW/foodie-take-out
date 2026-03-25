package com.foodie.dto.merchant;

import com.foodie.entity.DishFlavor;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DishDTO implements Serializable {

    private Long id;
    private String name;              // 菜品名称
    private Long categoryId;          // 分类ID
    private BigDecimal price;         // 菜品价格
    private String image;             // 图片路径
    private String description;       // 菜品描述
    private List<DishFlavor> flavors; // 口味列表
}