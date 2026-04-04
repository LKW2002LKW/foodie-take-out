package com.foodie.dto.user;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class MerchantQueryDTO implements Serializable {

    private String name;          // 商户名称（模糊搜索）
    private Long categoryId;      // 分类ID
    private BigDecimal longitude; // 用户当前经度
    private BigDecimal latitude;  // 用户当前纬度
    private Integer sortType;     // 排序类型：0-综合排序 1-距离优先 2-销量优先 3-评分优先 4-起送价最低 5-配送费最低
    private Integer page;         // 页码
    private Integer pageSize;     // 每页记录数
}
