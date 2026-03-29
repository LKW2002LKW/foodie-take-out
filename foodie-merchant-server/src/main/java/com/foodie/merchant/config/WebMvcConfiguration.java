package com.foodie.merchant.config;

import com.foodie.common.config.AbstractWebMvcConfiguration;
import com.foodie.common.config.WebMvcModuleMetadata;
import com.foodie.merchant.interceptor.JwtTokenMerchantInterceptor;
import org.springframework.context.annotation.Configuration;

/**
 * Web配置类：注册拦截器、配置消息转换器等
 */
@Configuration
public class WebMvcConfiguration extends AbstractWebMvcConfiguration {

    public WebMvcConfiguration(JwtTokenMerchantInterceptor jwtTokenMerchantInterceptor) {
        super(new WebMvcModuleMetadata(
                "com.foodie.merchant.controller",
                "吃货联盟商户管理端接口文档",
                "吃货联盟商户管理端接口文档",
                new String[]{"http://localhost:5177"},
                jwtTokenMerchantInterceptor,
                new String[]{"/merchant/**"},
                new String[]{
                        "/merchant/admin/login",
                        "/merchant/admin/register"
                }
        ));
    }
}
