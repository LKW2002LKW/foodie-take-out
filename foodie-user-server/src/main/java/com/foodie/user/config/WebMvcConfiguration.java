package com.foodie.user.config;

import com.foodie.common.config.AbstractWebMvcConfiguration;
import com.foodie.common.config.WebMvcModuleMetadata;
import com.foodie.user.interceptor.JwtTokenUserInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

/**
 * Web配置类
 */
@Configuration
@Slf4j
public class WebMvcConfiguration extends AbstractWebMvcConfiguration {

    private final JwtTokenUserInterceptor jwtTokenUserInterceptor;

    public WebMvcConfiguration(JwtTokenUserInterceptor jwtTokenUserInterceptor) {
        super(new WebMvcModuleMetadata(
                "com.foodie.user.controller",
                "吃货联盟用户端接口文档",
                "吃货联盟用户端接口文档",
                new String[]{"http://localhost:5177"}
        ));
        this.jwtTokenUserInterceptor = jwtTokenUserInterceptor;
    }

    /**
     * 注册自定义拦截器
     */
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");

        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**")
                .excludePathPatterns(
                        "/user/code",           // 发送验证码
                        "/user/register",       // 注册
                        "/user/login",          // 登录
                        "/user/wechat/login"    // 微信登录
                );
    }
}
