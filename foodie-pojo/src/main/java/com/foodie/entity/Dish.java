package com.foodie.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品表
 * 关联商户和分类，用于管理菜品信息
 */
@Data
@TableName("dish")
public class Dish implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 商户ID */
    private Long merchantId;

    /** 分类ID */
    private Long categoryId;

    /** 菜品名称 */
    private String name;

    /** 菜品价格 */
    private BigDecimal price;

    /** 图片路径 */
    private String image;

    /** 菜品描述 */
    private String description;

    /**
     * 售卖状态
     * 1 起售
     * 0 停售
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