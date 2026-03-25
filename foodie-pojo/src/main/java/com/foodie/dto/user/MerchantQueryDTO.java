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
    private Integer sortType;     // 排序类型：1-距离最近 2-销量最高 3-评分最高
    private Integer page;         // 页码
    private Integer pageSize;     // 每页记录数
}
