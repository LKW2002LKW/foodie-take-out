package com.foodie.merchant.mapper;

import com.foodie.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

/**
 * @Author: wanderer
 * @Date: 2026/1/28 17:24
 */
@Mapper
public interface SystemConfigMapper {
    @Select("""
    SELECT config_value, config_type
    FROM system_config
    WHERE config_key = #{key}
    LIMIT 1
""")
    SystemConfig selectByKey(@Param("key") String key);

}
