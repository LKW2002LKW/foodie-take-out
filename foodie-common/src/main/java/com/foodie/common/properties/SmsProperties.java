package com.foodie.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "foodie.sms")
public class SmsProperties {

    private String accessKeyId;
    private String accessKeySecret;
    private String signName;
    private String templateCode;
}
