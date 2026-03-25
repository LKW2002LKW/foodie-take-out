package com.foodie.vo.user;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class DishItemVO implements Serializable {
    private Long id;
    private String name;
    private BigDecimal price;
}

