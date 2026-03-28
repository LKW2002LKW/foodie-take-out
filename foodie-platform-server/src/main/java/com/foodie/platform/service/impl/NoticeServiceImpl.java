package com.foodie.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.foodie.dto.platform.NoticeDTO;
import com.foodie.entity.SystemNotice;
import com.foodie.common.exception.BaseException;
import com.foodie.platform.mapper.SystemNoticeMapper;
import com.foodie.platform.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 公告服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final SystemNoticeMapper noticeMapper;

    /**
     * 公告分页查询
     */
    @Override
    public Page<SystemNotice> pageQuery(Integer page, Integer pageSize, Integer status) {
        Page<SystemNotice> noticePage = new Page<>(page, pageSize);

        LambdaQueryWrapper<SystemNotice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(status != null, SystemNotice::getStatus, status)
                .orderByDesc(SystemNotice::getCreateTime);

        return noticeMapper.selectPage(noticePage, queryWrapper);
    }

    /**
     * 发布公告
     */
    @Override
    @Transactional
    public void publishNotice(NoticeDTO noticeDTO) {
        SystemNotice notice = new SystemNotice();
        BeanUtils.copyProperties(noticeDTO, notice);
        notice.setStatus(SystemNotice.STATUS_PUBLISHED);
        notice.setPublishTime(LocalDateTime.now());

        noticeMapper.insert(notice);
        log.info("发布公告成功：{}", notice.getTitle());
    }

    /**
     * 保存草稿
     */
    @Override
    @Transactional
    public void saveDraft(NoticeDTO noticeDTO) {
        SystemNotice notice = new SystemNotice();
        BeanUtils.copyProperties(noticeDTO, notice);
        notice.setStatus(SystemNotice.STATUS_DRAFT);

        noticeMapper.insert(notice);
        log.info("保存公告草稿成功：{}", notice.getTitle());
    }

    /**
     * 删除公告
     */
    @Override
    @Transactional
    public void deleteNotice(Long id) {
        SystemNotice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BaseException("公告不存在");
        }

        noticeMapper.deleteById(id);
        log.info("删除公告成功，ID：{}", id);
    }
}