package com.foodie.dto.platform;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 报表查询DTO
 */
@Data
public class ReportQueryDTO implements Serializable {

    /**
     * 开始日期
     */
    private LocalDate beginDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;
}