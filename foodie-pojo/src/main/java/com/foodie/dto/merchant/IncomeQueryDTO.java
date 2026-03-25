package com.foodie.dto.merchant;

/**
 * @Author: wanderer
 * @Date: 2026/1/22 11:24
 */
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 收入明细查询DTO
 */
@Data
public class IncomeQueryDTO implements Serializable {

    private Integer page = 1;
    private Integer pageSize = 10;

    /**
     * 开始日期
     */
    private LocalDate beginDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 订单号
     */
    private String orderNumber;
}
