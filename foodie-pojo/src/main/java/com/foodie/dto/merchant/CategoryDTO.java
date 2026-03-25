package com.foodie.dto.merchant;

import lombok.Data;
import java.io.Serializable;

@Data
public class CategoryDTO implements Serializable {

    private Long id;
    private String name;       // 分类名称
    private Integer type;      // 分类类型 1菜品分类 2套餐分类
    private Integer sort;      // 排序
}