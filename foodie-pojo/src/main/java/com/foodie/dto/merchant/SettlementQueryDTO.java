package com.foodie.dto.merchant;

/**
 * @Author: wanderer
 * @Date: 2026/1/22 11:23
 */
import lombok.Data;
import java.io.Serializable;

/**
 * 结算记录查询DTO
 */
@Data
public class SettlementQueryDTO implements Serializable {

    private Integer page = 1;
    private Integer pageSize = 10;

    /**
     * 结算周期（如：2026-01）
     */
    private String settlementCycle;

    /**
     * 结算状态 1待结算 2已结算
     */
    private Integer status;
}