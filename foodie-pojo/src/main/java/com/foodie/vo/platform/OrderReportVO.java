package com.foodie.vo.platform;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 订单统计报表VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderReportVO implements Serializable {

    /**
     * 日期列表
     */
    private List<String> dateList;

    /**
     * 订单数列表
     */
    private List<Integer> orderCountList;

    /**
     * 有效订单数列表
     */
    private List<Integer> validOrderCountList;

    /**
     * 总订单数
     */
    private Integer totalOrderCount;

    /**
     * 有效订单数
     */
    private Integer validOrderCount;

    /**
     * 订单完成率
     */
    private Double orderCompletionRate;
}