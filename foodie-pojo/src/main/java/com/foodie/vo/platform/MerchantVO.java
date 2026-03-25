package com.foodie.vo.platform;

import lombok.Data;
import java.io.Serializable;

@Data
public class MerchantVO implements Serializable {
    private Long id;
    private String name;
    private String phone;
    private String status;
}

