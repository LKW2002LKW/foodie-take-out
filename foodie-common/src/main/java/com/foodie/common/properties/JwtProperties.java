package com.foodie.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "foodie.jwt")
@Data
public class JwtProperties {
    private String merchantSecretKey;
    private Long merchantTtl;
    private String merchantTokenName;

    private String platformSecretKey;
    private Long platformTtl;
    private String platformTokenName;

    private String userSecretKey;
    private Long userTtl;
    private String userTokenName;
}