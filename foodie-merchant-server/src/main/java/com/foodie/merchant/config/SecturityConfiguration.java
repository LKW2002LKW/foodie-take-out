package com.foodie.merchant.config;

import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author wanderer
 * @since 2026-01-09
 */

@Configuration
@EnableWebSecurity
public class SecturityConfiguration  {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // 关闭 CSRF
                .csrf().disable()

                // 开启 CORS
                .cors()

                // 权限配置
                .and() // <- ensure we return to HttpSecurity before calling authorizeRequests()
                .authorizeRequests()
                // 放行预检请求 OPTIONS
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 放行商户登录接口
                .antMatchers("/merchant/**").permitAll()
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

                // 其他接口都需要认证
                .anyRequest().authenticated();

        return http.build();
    }

    /**
     * CORS 配置（用于 CorsConfigurationSource）
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // DEBUG: 放宽为允许所有来源
        config.setAllowedOriginPatterns(Collections.singletonList("http://localhost:5176"));
        config.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS"));
        // 允许所有请求头
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * 注册一个优先级最高的 CorsFilter，确保在 Spring Security 之前处理 CORS 预检
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        // DEBUG: 放宽为允许所有来源
        config.setAllowedOriginPatterns(Collections.singletonList("*"));
        config.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

}
