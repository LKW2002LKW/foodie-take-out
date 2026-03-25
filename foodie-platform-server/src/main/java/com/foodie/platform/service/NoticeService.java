package com.foodie.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodie.dto.platform.NoticeDTO;
import com.foodie.entity.SystemNotice;

/**
 * 公告服务接口
 */
public interface NoticeService {

    /**
     * 公告分页查询
     */
    Page<SystemNotice> pageQuery(Integer page, Integer pageSize, Integer status);

    /**
     * 发布公告
     */
    void publishNotice(NoticeDTO noticeDTO);

    /**
     * 保存草稿
     */
    void saveDraft(NoticeDTO noticeDTO);

    /**
     * 删除公告
     */
    void deleteNotice(Long id);
}