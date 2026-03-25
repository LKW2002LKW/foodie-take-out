package com.foodie.platform.service;



import com.foodie.dto.platform.ConfigDTO;
import com.foodie.entity.SystemConfig;

import java.util.List;

/**
 * 配置服务接口
 */
public interface ConfigService {

    /**
     * 查询所有配置
     */
    List<SystemConfig> listAll();

    /**
     * 根据Key查询配置
     */
    SystemConfig getByKey(String configKey);

    /**
     * 更新配置
     */
    void updateConfig(String configKey, String configValue);

    /**
     * 批量更新配置
     */
    void batchUpdate(List<ConfigDTO> configList);
}