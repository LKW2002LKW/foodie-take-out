package com.foodie.platform.config;

import com.foodie.common.config.AbstractWebMvcConfiguration;
import com.foodie.common.config.WebMvcModuleMetadata;
import com.foodie.platform.interceptor.JwtTokenPlatformInterceptor;
import org.springframework.context.annotation.Configuration;

/**
 * Web配置类：注册拦截器、配置消息转换器等
 */
@Configuration
public class WebMvcConfiguration extends AbstractWebMvcConfiguration {

    public WebMvcConfiguration(JwtTokenPlatformInterceptor jwtTokenPlatformInterceptor) {
        super(new WebMvcModuleMetadata(
                "com.foodie.platform.controller",
                "吃货联盟平台端接口文档",
                "吃货联盟平台端接口文档",
                new String[]{"http://localhost:5178"},
                jwtTokenPlatformInterceptor,
                new String[]{"/platform/**"},
                new String[]{"/platform/admin/login"}
        ));
    }
}
