package com.foodie.merchant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodie.entity.SystemNotice;
import com.foodie.pojo.dto.NoticeQueryDTO;

import com.foodie.pojo.vo.SystemNoticeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统公告Mapper
 */
@Mapper
public interface SystemNoticeMapper extends BaseMapper<SystemNotice> {

    /**
     * 分页查询公告列表（商户端）
     */
    List<SystemNoticeVO> selectNoticePage(@Param("query") NoticeQueryDTO query);

    /**
     * 查询公告总数
     */
    Long countNotices(@Param("query") NoticeQueryDTO query);
}