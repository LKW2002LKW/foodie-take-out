package com.foodie.vo.platform;

/**
 * @Author: wanderer
 * @Date: 2026/1/15 13:38
 */
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 趋势数据VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendDataVO implements Serializable {

    /**
     * 日期列表
     */
    private List<String> dateList;

    /**
     * 订单量列表
     */
    private List<Integer> orderCountList;

    /**
     * 交易额列表
     */
    private List<BigDecimal> revenueList;

    /**
     * 用户数列表
     */
    private List<Integer> userCountList;
}
