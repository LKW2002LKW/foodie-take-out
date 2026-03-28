package com.foodie.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "foodie.upload")
public class UploadModuleProperties {

    private String servicePath;
}
