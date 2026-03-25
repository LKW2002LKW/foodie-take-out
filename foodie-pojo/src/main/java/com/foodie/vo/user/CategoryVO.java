package com.foodie.vo.user;

import lombok.Data;
import java.io.Serializable;

@Data
public class CategoryVO implements Serializable {

    private Long id;
    private String name;
    private Integer type;        // 1-菜品分类 2-套餐分类
    private Integer dishCount;   // 该分类下的菜品数量
}