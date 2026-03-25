package com.foodie.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security / OAuth2 configuration skeleton
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // 1️⃣ 前后端分离，关闭 CSRF
                .csrf().disable()

                // 2️⃣ 不使用 Session
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()

                // 3️⃣ 关闭默认表单登录（否则就会 302 /login）
                .formLogin().disable()
                .httpBasic().disable()

                // 额外开启 CORS 支持（从前端发起跨域请求时需要）
                .cors()

                // 4️⃣ 权限规则
                .and()
                .authorizeRequests()
                // ✅ 放行预检请求
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // ✅ 放行短信验证码接口
                .antMatchers("/user/**").permitAll()
                //静态图片资源访问
                .antMatchers("/images/**").permitAll()

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

                // 其他接口需要认证
                .anyRequest().authenticated();

        return http.build();
    }
}
