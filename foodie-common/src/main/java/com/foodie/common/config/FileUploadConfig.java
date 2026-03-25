package com.foodie.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 文件上传配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadConfig {

    /**
     * 文件上传路径
     */
    private String path = "/data/uploads/";

    /**
     * 访问路径前缀
     */
    private String urlPrefix = "http://localhost:8083";

    /**
     * 单个文件最大大小（MB）
     */
    private Long maxSize = 5L;

    /**
     * 允许上传的文件类型
     */
    private List<String> allowedTypes = List.of("jpg", "jpeg", "png", "gif", "webp");

    /**
     * 评价图片最大数量
     */
    private Integer reviewMaxImages = 9;

    /**
     * 获取完整的上传路径
     */
    public String getFullPath() {
        return path.endsWith("/") ? path : path + "/";
    }

    /**
     * 获取完整的URL前缀
     */
    public String getFullUrlPrefix() {
        return urlPrefix.endsWith("/") ? urlPrefix.substring(0, urlPrefix.length() - 1) : urlPrefix;
    }
}