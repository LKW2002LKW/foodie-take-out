package com.foodie.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统公告实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("system_notice")
public class SystemNotice implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 公告类型常量
     */
    public static final Integer TYPE_SYSTEM = 1;    // 系统公告
    public static final Integer TYPE_ACTIVITY = 2;  // 活动公告

    /**
     * 目标对象常量
     */
    public static final Integer TARGET_MERCHANT = 1;  // 商户
    public static final Integer TARGET_USER = 2;      // 用户
    public static final Integer TARGET_ALL = 3;       // 全部

    /**
     * 状态常量
     */
    public static final Integer STATUS_PUBLISHED = 1;  // 已发布
    public static final Integer STATUS_DRAFT = 0;      // 草稿

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 公告类型 1系统公告 2活动公告
     */
    private Integer type;

    /**
     * 目标对象 1商户 2用户 3全部
     */
    private Integer target;

    /**
     * 状态 1已发布 0草稿
     */
    private Integer status;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Long createUser;
}