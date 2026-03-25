package com.foodie.dto.platform;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 抽成配置DTO
 */
@Data
public class CommissionConfigDTO implements Serializable {

    /**
     * 配置类型 1全局默认 2商户特定 3分类特定
     */
    @NotNull(message = "配置类型不能为空")
    private Integer configType;

    /**
     * 商户ID（商户特定时必填）
     */
    private Long merchantId;

    /**
     * 分类ID（分类特定时必填）
     */
    private Long categoryId;

    /**
     * 抽成比例
     */
    @NotNull(message = "抽成比例不能为空")
    private BigDecimal commissionRate;

    /**
     * 生效时间
     */
    private LocalDateTime effectiveTime;

    /**
     * 失效时间
     */
    private LocalDateTime expireTime;
}