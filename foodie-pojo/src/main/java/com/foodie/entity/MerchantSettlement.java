package com.foodie.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor


/**
 * 商户结算表
 */

@TableName("merchant_settlement")
public class MerchantSettlement implements Serializable{

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 商户ID */
    private Long merchantId;

    /** 结算周期（如：2025-01 / 2025-01-01~2025-01-07） */
    private String settlementCycle;

    /** 订单数量 */
    private Integer orderCount;

    /** 订单总额 */
    private BigDecimal totalAmount;

    /** 平台抽成金额 */
    private BigDecimal platformCommission;

    /** 商户应得金额 */
    private BigDecimal merchantAmount;

    /** 实际结算金额 */
    private BigDecimal settlementAmount;

    /**
     * 结算状态
     * 1 待结算
     * 2 已结算
     * 3 已取消
     */
    private Integer status;

    /** 结算时间 */
    private LocalDateTime settlementTime;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}


