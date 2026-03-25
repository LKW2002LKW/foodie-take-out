package com.foodie.vo.platform;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 营业额报表VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportVO implements Serializable {

    /**
     * 日期列表
     */
    private List<String> dateList;

    /**
     * 营业额列表
     */
    private List<BigDecimal> revenueList;

    /**
     * 总营业额
     */
    private BigDecimal totalRevenue;
}