package com.foodie.platform.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // 关闭 CSRF（前后端分离必须）
                .csrf().disable()

                // 开启 CORS（配合 CorsConfigurationSource）
                .cors()

                .and()
                .authorizeRequests()
                // 1. 预检请求必须全部放行
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // 2. 登录接口放行
                //.antMatchers("/platform/admin/login").permitAll()

                // 3. 平台接口（你当前策略）
                .antMatchers("/platform/**").permitAll()
                // ===== 接口文档放行（关键）=====
                // ===== 接口文档放行（关键）=====
                .antMatchers(
                        "/doc.html",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/webjars/**",
                        "/favicon.ico",
                        "/swagger-resources",
                        "/v2/api-docs"
                ).permitAll()


                // 4. 错误页必须放行
                .antMatchers("/error").permitAll()

                // 5. 其他请求需要认证
                .anyRequest().authenticated()

                .and()
                // 未认证直接返回 401，不跳转登录页
                .exceptionHandling()
                .authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                );

        return http.build();
    }

    /**
     * Spring Security 使用的 CORS 配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(
                Collections.singletonList("*")
        );
        config.setAllowedMethods(
                Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")
        );
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
