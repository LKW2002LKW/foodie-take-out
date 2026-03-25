package com.foodie.dto.merchant;

import lombok.Data;
import java.io.Serializable;

@Data
public class DishPageQueryDTO implements Serializable {

    private Long merchantId;   // 商户ID（Service层设置）
    private String name;       // 菜品名称（模糊查询）
    private Long categoryId;   // 分类ID
    private Integer status;    // 状态
    private Integer page;      // 页码
    private Integer pageSize;  // 每页记录数
}