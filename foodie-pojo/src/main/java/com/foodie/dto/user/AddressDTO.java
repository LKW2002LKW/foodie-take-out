package com.foodie.dto.user;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class AddressDTO implements Serializable {

    private Long id;
    private String consignee;      // 收货人
    private Integer sex;           // 性别 0未知 1男 2女
    private String phone;          // 手机号
    private String provinceCode;   // 省份编码
    private String provinceName;   // 省份名称
    private String cityCode;       // 城市编码
    private String cityName;       // 城市名称
    private String districtCode;   // 区县编码
    private String districtName;   // 区县名称
    private String detail;         // 详细地址
    private String label;          // 地址标签：家、公司、学校
    private Integer isDefault;     // 是否默认 1是 0否（可选）
    private BigDecimal longitude;  // 经度
    private BigDecimal latitude;   // 纬度
}