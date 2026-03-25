package com.foodie.dto.platform;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 商户审核DTO
 */
@Data
public class MerchantAuditDTO implements Serializable {

    @NotNull(message = "商户ID不能为空")
    private Long id;

    /**
     * 审核状态 1通过 3拒绝
     */
    @NotNull(message = "审核状态不能为空")
    private Integer auditStatus;

    /**
     * 审核说明
     */
    private String auditReason;
}