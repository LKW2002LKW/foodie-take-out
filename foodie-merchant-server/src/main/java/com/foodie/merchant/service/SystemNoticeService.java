package com.foodie.merchant.service;

import com.foodie.pojo.dto.NoticeQueryDTO;
import com.foodie.pojo.vo.SystemNoticeVO;
import java.util.Map;

/**
 * 系统公告服务接口
 */
public interface SystemNoticeService {

    /**
     * 分页查询公告列表
     */
    Map<String, Object> getNoticePage(NoticeQueryDTO query);

    /**
     * 查询公告详情
     */
    SystemNoticeVO getNoticeDetail(Long noticeId);
}