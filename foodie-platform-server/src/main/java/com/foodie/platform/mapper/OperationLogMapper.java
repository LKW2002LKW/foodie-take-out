package com.foodie.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodie.common.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志Mapper
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {

}