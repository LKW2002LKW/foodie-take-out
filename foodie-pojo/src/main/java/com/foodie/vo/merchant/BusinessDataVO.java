package com.foodie.vo.merchant;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class BusinessDataVO implements Serializable {
    private long totalOrders;
    private BigDecimal totalRevenue;
}

