package com.foodie.dto.merchant;

/**
 * @Author: wanderer
 * @Date: 2026/1/20 11:54
 */

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 统计查询DTO
 */
@Data
public class StatisticsQueryDTO implements Serializable {

    /**
     * 开始日期
     */
    private LocalDate beginDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 天数（7或30）
     */
    private Integer days;
}