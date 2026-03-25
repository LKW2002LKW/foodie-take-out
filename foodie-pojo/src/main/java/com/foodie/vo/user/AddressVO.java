package com.foodie.vo.user;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class AddressVO implements Serializable {

    private Long id;
    private Long userId;
    private String consignee;
    private Integer sex;
    private String phone;
    private String provinceCode;
    private String provinceName;
    private String cityCode;
    private String cityName;
    private String districtCode;
    private String districtName;
    private String detail;
    private String label;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer isDefault;     // 是否默认 1是 0否

    // 完整地址（拼接）
    private String fullAddress;
}