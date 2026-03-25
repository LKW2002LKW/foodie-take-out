package com.foodie.vo.platform;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OrderStatisticsVO implements Serializable {
    private long totalOrders;
    private BigDecimal totalRevenue;
}

