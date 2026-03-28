package com.foodie.merchant.config;

import com.foodie.common.config.AbstractWebMvcConfiguration;
import com.foodie.common.config.WebMvcModuleMetadata;
import com.foodie.merchant.interceptor.JwtTokenMerchantInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

/**
 * Web配置类：注册拦截器、配置消息转换器等
 */
@Configuration
@Slf4j
public class WebMvcConfiguration extends AbstractWebMvcConfiguration {

    private final JwtTokenMerchantInterceptor jwtTokenMerchantInterceptor;

    public WebMvcConfiguration(JwtTokenMerchantInterceptor jwtTokenMerchantInterceptor) {
        super(new WebMvcModuleMetadata(
                "com.foodie.merchant.controller",
                "吃货联盟商户管理端接口文档",
                "吃货联盟商户管理端接口文档",
                new String[]{"http://localhost:5177"}
        ));
        this.jwtTokenMerchantInterceptor = jwtTokenMerchantInterceptor;
    }

    /**
     * 注册自定义拦截器
     */
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");

        registry.addInterceptor(jwtTokenMerchantInterceptor)
                .addPathPatterns("/merchant/**")
                .excludePathPatterns(
                        "/merchant/admin/login",     // 登录接口不拦截
                        "/merchant/admin/register"   // 注册接口不拦截
                );
    }
}
