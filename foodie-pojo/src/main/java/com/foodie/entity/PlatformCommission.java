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
 * 平台抽成配置表
 */

@TableName("platform_commission")
public class PlatformCommission implements Serializable {

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 配置类型
     * 1 全局默认
     * 2 商户特定
     * 3 分类特定
     */
    private Integer configType;

    /** 商户ID（configType = 2 时有效） */
    private Long merchantId;

    /** 分类ID（configType = 3 时有效） */
    private Long categoryId;

    /** 抽成比例（百分比，如 15.00） */
    private BigDecimal commissionRate;

    /** 生效时间 */
    private LocalDateTime effectiveTime;

    /** 失效时间 */
    private LocalDateTime expireTime;

    /**
     * 状态
     * 1 启用
     * 0 禁用
     */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 创建人ID */
    private Long createUser;

    /** 更新人ID */
    private Long updateUser;
}


