package com.foodie.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "foodie.minio")
public class MinioProperties {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String publicUrl;
    private String basePath;
    private Long maxSize = 5L;
    private List<String> allowedTypes = List.of("jpg", "jpeg", "png", "gif", "webp");
}
