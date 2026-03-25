package com.foodie.platform.mapper;

/**
 * @Author: wanderer
 * @Date: 2026/1/15 14:37
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.foodie.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 用户Mapper（平台端）
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 统计今日新增用户数
     */
    @Select("SELECT COUNT(*) FROM user WHERE DATE(create_time) = CURDATE()")
    Integer countTodayNew();
}