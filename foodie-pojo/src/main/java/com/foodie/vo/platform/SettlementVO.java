package com.foodie.vo.platform;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 结算单VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementVO implements Serializable {

    private Long id;
    private Long merchantId;
    private String merchantName;
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