package com.foodie.dto.merchant;

import lombok.Data;
import java.io.Serializable;

@Data
public class CategoryPageQueryDTO implements Serializable {

    private String name;       // 分类名称（模糊查询）
    private Integer type;      // 分类类型
    private Integer page;      // 页码
    private Integer pageSize;  // 每页记录数
}