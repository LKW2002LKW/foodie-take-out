package com.foodie.vo.merchant;

/**
 * @Author: wanderer
 * @Date: 2026/1/22 11:26
 */
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 结算记录VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementRecordVO implements Serializable {

    private Long id;
    private String settlementCycle;
    private Integer orderCount;
    private BigDecimal totalAmount;
    private BigDecimal platformCommission;
    private BigDecimal merchantAmount;
    private BigDecimal settlementAmount;
    private Integer status;
    private String statusText;
    private LocalDateTime settlementTime;
    private String remark;
    private LocalDateTime createTime;
}