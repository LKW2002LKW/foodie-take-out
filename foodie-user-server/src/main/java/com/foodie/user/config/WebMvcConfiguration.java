package com.foodie.user.config;

import com.foodie.common.config.AbstractWebMvcConfiguration;
import com.foodie.common.config.WebMvcModuleMetadata;
import com.foodie.user.interceptor.JwtTokenUserInterceptor;
import org.springframework.context.annotation.Configuration;

/**
 * Web配置类
 */
@Configuration
public class WebMvcConfiguration extends AbstractWebMvcConfiguration {

    public WebMvcConfiguration(JwtTokenUserInterceptor jwtTokenUserInterceptor) {
        super(new WebMvcModuleMetadata(
                "com.foodie.user.controller",
                "吃货联盟用户端接口文档",
                "吃货联盟用户端接口文档",
                new String[]{"http://localhost:5177"},
                jwtTokenUserInterceptor,
                new String[]{"/user/**"},
                new String[]{
                        "/user/code",
                        "/user/register",
                        "/user/login",
                        "/user/wechat/login"
                }
        ));
    }
}
