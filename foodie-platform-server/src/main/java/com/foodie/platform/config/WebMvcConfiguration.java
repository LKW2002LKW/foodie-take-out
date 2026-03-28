package com.foodie.platform.config;

import com.foodie.common.config.AbstractWebMvcConfiguration;
import com.foodie.platform.interceptor.JwtTokenPlatformInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

/**
 * Web配置类：注册拦截器、配置消息转换器等
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class WebMvcConfiguration extends AbstractWebMvcConfiguration {

    private final JwtTokenPlatformInterceptor jwtTokenPlatformInterceptor;

    @Override
    protected String getSwaggerBasePackage() {
        return "com.foodie.platform.controller";
    }

    @Override
    protected String getSwaggerTitle() {
        return "吃货联盟平台端接口文档";
    }

    @Override
    protected String getSwaggerDescription() {
        return "吃货联盟平台端接口文档";
    }

    @Override
    protected String[] getAllowedOrigins() {
        return new String[]{"http://localhost:5178"};
    }

    /**
     * 注册自定义拦截器
     */
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");

        registry.addInterceptor(jwtTokenPlatformInterceptor)
                .addPathPatterns("/platform/**")
                .excludePathPatterns(
                        "/platform/admin/login"    // 登录接口不拦截

                );
    }
}
