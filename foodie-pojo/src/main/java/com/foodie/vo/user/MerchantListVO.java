package com.foodie.vo.user;

import lombok.Data;
import java.io.Serializable;

@Data
public class MerchantListVO implements Serializable {
    private Long id;
    private String name;
    private String distance;
}

