package com.foodie.vo.platform;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 热销商品VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotDishVO implements Serializable {

    private Long dishId;
    private String dishName;
    private String dishImage;
    private Long merchantId;
    private String merchantName;
    private Integer salesCount;
    private Double salesRatio;
}