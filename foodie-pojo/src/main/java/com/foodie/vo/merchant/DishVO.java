package com.foodie.vo.merchant;

import com.foodie.entity.DishFlavor;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DishVO implements Serializable {

    private Long id;
    private Long merchantId;
    private Long categoryId;
    private String categoryName;      // 分类名称
    private String name;
    private BigDecimal price;
    private String image;
    private String description;
    private Integer status;
    private String statusDesc;        // 状态描述
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<DishFlavor> flavors; // 口味列表
}