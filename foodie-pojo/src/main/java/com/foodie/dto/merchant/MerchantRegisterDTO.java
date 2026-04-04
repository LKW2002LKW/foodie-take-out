package com.foodie.dto.merchant;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class MerchantRegisterDTO implements Serializable {

    // 商户信息
    private String merchantName;
    private String businessLicense;
    private String legalPerson;
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
    private String description;
    private Long bizCategoryId;
    private String businessHours;
    private BigDecimal minDeliveryAmount;
    private BigDecimal deliveryFee;

    // 管理员信息
    private String username;
    private String password;
    private String name;
    private String phone;
}