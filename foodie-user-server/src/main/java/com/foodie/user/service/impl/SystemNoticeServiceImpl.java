package com.foodie.user.service.impl;

import com.foodie.common.constant.NoticeConstant;
import com.foodie.common.exception.BusinessException;
import com.foodie.user.mapper.SystemNoticeMapper;
import com.foodie.user.service.SystemNoticeService;
import com.foodie.pojo.dto.NoticeQueryDTO;
import com.foodie.entity.SystemNotice;
import com.foodie.pojo.vo.SystemNoticeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统公告服务实现类（用户端）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemNoticeServiceImpl implements SystemNoticeService {

    private final SystemNoticeMapper systemNoticeMapper;

    @Override
    public Map<String, Object> getNoticePage(NoticeQueryDTO query) {
        // 查询公告列表
        List<SystemNoticeVO> notices = systemNoticeMapper.selectNoticePage(query);

        // 处理数据
        notices.forEach(notice -> {
            // 设置类型名称
            notice.setTypeName(NoticeConstant.getTypeName(notice.getType()));
            notice.setTargetName(NoticeConstant.getTargetName(notice.getTarget()));
        });

        // 查询总数
        Long total = systemNoticeMapper.countNotices(query);

        // 计算总页数
        int totalPages = (int) Math.ceil((double) total / query.getPageSize());

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", notices);
        result.put("total", total);
        result.put("page", query.getPage());
        result.put("pageSize", query.getPageSize());
        result.put("totalPages", totalPages);
        result.put("hasNext", query.getPage() < totalPages);

        return result;
    }

    @Override
    public SystemNoticeVO getNoticeDetail(Long noticeId) {
        if (noticeId == null) {
            throw new BusinessException("公告ID不能为空");
        }

        // 查询公告
        SystemNotice notice = systemNoticeMapper.selectById(noticeId);

        if (notice == null) {
            throw new BusinessException("公告不存在");
        }

        // 检查状态和目标对象
        if (notice.getStatus() != NoticeConstant.STATUS_PUBLISHED) {
            throw new BusinessException("公告未发布");
        }

        if (notice.getTarget() != NoticeConstant.TARGET_USER
                && notice.getTarget() != NoticeConstant.TARGET_ALL) {
            throw new BusinessException("无权查看此公告");
        }

        // 转换为VO
        SystemNoticeVO vo = new SystemNoticeVO();
        BeanUtils.copyProperties(notice, vo);

        // 设置类型名称
        vo.setTypeName(NoticeConstant.getTypeName(notice.getType()));
        vo.setTargetName(NoticeConstant.getTargetName(notice.getTarget()));

        return vo;
    }
}