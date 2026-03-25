package com.foodie.dto.platform;

/**
 * @Author: wanderer
 * @Date: 2026/1/16 16:40
 */
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 生成结算单DTO
 */
@Data
public class SettlementGenerateDTO implements Serializable {

    /**
     * 结算周期（如：2026-01）
     */
    @NotBlank(message = "结算周期不能为空")
    private String settlementCycle;

    /**
     * 商户ID列表（为空则生成全部）
     */
    private List<Long> merchantIds;
}