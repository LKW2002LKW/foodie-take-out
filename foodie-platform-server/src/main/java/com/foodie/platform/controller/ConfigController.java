package com.foodie.platform.controller;


import com.foodie.common.result.Result;
import com.foodie.dto.platform.ConfigDTO;
import com.foodie.entity.SystemConfig;
import com.foodie.platform.service.ConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 配置控制器
 */
@RestController
@RequestMapping("/platform/config")
@Api(tags = "平台端-系统配置接口")
@Slf4j
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;

    /**
     * 查询所有配置
     */
    @GetMapping("/list")
    @ApiOperation("查询所有配置")
    public Result<List<SystemConfig>> listAll() {
        log.info("查询所有配置");
        List<SystemConfig> configs = configService.listAll();
        return Result.success(configs);
    }

    /**
     * 根据Key查询配置
     */
    @GetMapping("/{configKey}")
    @ApiOperation("根据Key查询配置")
    public Result<SystemConfig> getByKey(@PathVariable String configKey) {
        log.info("查询配置，Key：{}", configKey);
        SystemConfig config = configService.getByKey(configKey);
        return Result.success(config);
    }

    /**
     * 更新配置
     */
    @PutMapping
    @ApiOperation("更新配置")
    public Result<Void> updateConfig(@RequestParam String configKey,
                                     @RequestParam String configValue) {
        log.info("更新配置，Key：{}，Value：{}", configKey, configValue);
        configService.updateConfig(configKey, configValue);
        return Result.success();
    }

    /**
     * 批量更新配置
     */
    @PutMapping("/batch")
    @ApiOperation("批量更新配置")
    public Result<Void> batchUpdate(@RequestBody List<ConfigDTO> configList) {
        log.info("批量更新配置，数量：{}", configList.size());
        configService.batchUpdate(configList);
        return Result.success();
    }
}