package com.foodie.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分类表
 * 用于商户的菜品或套餐分类
 */
@Data
@TableName("category")
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 商户ID */
    private Long merchantId;

    /** 分类名称 */
    private String name;

    /**
     * 分类类型
     * 1 菜品分类
     * 2 套餐分类
     */
    private Integer type;

    /** 排序字段 */
    private Integer sort;

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