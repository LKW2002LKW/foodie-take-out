package com.foodie.dto.platform;

/**
 * @Author: wanderer
 * @Date: 2026/1/16 16:42
 */

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 结算审核DTO
 */
@Data
public class SettlementAuditDTO implements Serializable {

    @NotNull(message = "结算单ID不能为空")
    private Long id;

    /**
     * 实际结算金额
     */
    private BigDecimal settlementAmount;

    /**
     * 备注
     */
    private String remark;
}
