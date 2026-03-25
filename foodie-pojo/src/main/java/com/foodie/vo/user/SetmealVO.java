package com.foodie.vo.user;

import com.foodie.entity.SetmealDish;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SetmealVO implements Serializable {

    private Long id;
    private Long merchantId;
    private Long categoryId;
    private String name;
    private BigDecimal price;
    private String image;
    private String description;
    private List<SetmealDish> setmealDishes;  // 套餐菜品列表
}