package com.foodie.platform.mapper;

/**
 * @Author: wanderer
 * @Date: 2026/1/16 16:50
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.foodie.entity.PlatformCommission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 平台抽成配置Mapper
 */
@Mapper
public interface PlatformCommissionMapper extends BaseMapper<PlatformCommission> {

}