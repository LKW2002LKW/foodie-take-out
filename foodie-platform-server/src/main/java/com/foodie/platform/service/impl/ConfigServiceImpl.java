package com.foodie.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.foodie.common.exception.BaseException;
import com.foodie.dto.platform.ConfigDTO;
import com.foodie.entity.SystemConfig;
import com.foodie.platform.mapper.SystemConfigMapper;
import com.foodie.platform.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 配置服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final SystemConfigMapper configMapper;

    /**
     * 查询所有配置
     */
    @Override
    public List<SystemConfig> listAll() {
        return configMapper.selectList(null);
    }

    /**
     * 根据Key查询配置
     */


    @Override
    public SystemConfig getByKey(String configKey) {
        LambdaQueryWrapper<SystemConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemConfig::getConfigKey, configKey);
        return configMapper.selectOne(queryWrapper);
    }

    /**
     * 更新配置
     */
    @Override
    @Transactional
    public void updateConfig(String configKey, String configValue) {
        SystemConfig config = getByKey(configKey);
        if (config == null) {
            throw new BaseException("配置不存在");
        }

        LambdaUpdateWrapper<SystemConfig> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SystemConfig::getConfigKey, configKey)
                .set(SystemConfig::getConfigValue, configValue);

        configMapper.update(null, updateWrapper);
        log.info("更新配置成功，Key：{}，Value：{}", configKey, configValue);
    }

    /**
     * 批量更新配置
     */
    @Override
    @Transactional
    public void batchUpdate(List<ConfigDTO> configList) {
        for (ConfigDTO configDTO : configList) {
            updateConfig(configDTO.getConfigKey(), configDTO.getConfigValue());
        }
        log.info("批量更新配置成功，数量：{}", configList.size());


    }

}