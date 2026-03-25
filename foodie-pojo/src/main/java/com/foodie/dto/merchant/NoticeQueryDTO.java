package com.foodie.pojo.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 公告查询DTO
 */
@Data
public class NoticeQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 公告类型 1系统公告 2活动公告
     */
    private Integer type;

    /**
     * 当前页码
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 获取分页起始位置
     */
    public Integer getOffset() {
        return (page - 1) * pageSize;
    }
}