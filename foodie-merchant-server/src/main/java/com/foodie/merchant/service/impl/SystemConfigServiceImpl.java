package com.foodie.merchant.service.impl;

import com.foodie.common.exception.BaseException;
import com.foodie.entity.SystemConfig;
import com.foodie.merchant.mapper.SystemConfigMapper;
import com.foodie.merchant.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @Author: wanderer
 * @Date: 2026/1/28 17:18
 */
@Service
@Slf4j
public class SystemConfigServiceImpl implements SystemConfigService {

    @Resource
    private SystemConfigMapper systemConfigMapper;

    @Override
    public BigDecimal getDecimal(String key) {
        SystemConfig config = systemConfigMapper.selectByKey(key);

        if (config == null) {
            throw new BaseException("系统配置不存在：" + key);
        }

        if (!"number".equalsIgnoreCase(config.getConfigType())) {
            throw new BaseException("系统配置类型不匹配：" + key);
        }

        try {
            return new BigDecimal(config.getConfigValue());
        } catch (NumberFormatException e) {
            throw new BaseException("系统配置值非法：" + key);
        }
    }

}

