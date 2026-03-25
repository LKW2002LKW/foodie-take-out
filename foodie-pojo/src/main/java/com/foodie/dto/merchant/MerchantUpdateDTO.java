package com.foodie.dto.merchant;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class MerchantUpdateDTO implements Serializable {

    private Long id;
    private String merchantName;
    private String contactName;
    private String contactPhone;
    private String provinceCode;
    private String provinceName;
    private String cityCode;
    private String cityName;
    private String districtCode;
    private String districtName;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String logo;
    private String description;
    private String businessHours;
    private BigDecimal minDeliveryAmount;
    private BigDecimal deliveryFee;
}