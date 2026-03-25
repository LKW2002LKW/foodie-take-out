package com.foodie.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统公告视图对象
 */
@Data
public class SystemNoticeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 公告ID
     */
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
     * 公告类型名称
     */
    private String typeName;

    /**
     * 目标对象 1商户 2用户 3全部
     */
    private Integer target;

    /**
     * 目标对象名称
     */
    private String targetName;

    /**
     * 状态 1已发布 0草稿
     */
    private Integer status;

    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}