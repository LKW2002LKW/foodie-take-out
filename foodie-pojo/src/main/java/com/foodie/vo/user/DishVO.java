package com.foodie.vo.user;

import com.foodie.entity.DishFlavor;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DishVO implements Serializable {

    private Long id;
    private Long merchantId;
    private Long categoryId;
    private String name;
    private BigDecimal price;
    private String image;
    private String description;
    private List<DishFlavor> flavors;  // 口味列表
}