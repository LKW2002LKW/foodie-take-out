package com.foodie.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.foodie.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

/**
 * 商户Mapper
 */
@Mapper
public interface MerchantMapper extends BaseMapper<Merchant> {

    /**
     * 统计今日新增商户数
     */
    @Select("SELECT COUNT(*) FROM merchant WHERE DATE(create_time) = CURDATE()")
    Integer countTodayNew();

    /**
     * 统计待审核商户数
     */
    @Select("SELECT COUNT(*) FROM merchant WHERE audit_status = 2")
    Integer countPending();

    /**
     * 统计营业中商户数
     */
    @Select("SELECT COUNT(*) FROM merchant WHERE status = 1")
    Integer countOpen();
}