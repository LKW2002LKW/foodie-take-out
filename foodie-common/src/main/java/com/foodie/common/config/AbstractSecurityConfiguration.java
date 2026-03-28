package com.foodie.common.config;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * 统一安全配置基类
 */
public abstract class AbstractSecurityConfiguration {

    protected String[] getSwaggerPermitAllPatterns() {
        return new String[]{
                "/doc.html",
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/webjars/**",
                "/favicon.ico",
                "/swagger-resources",
                "/v2/api-docs"
        };
    }

    protected abstract String[] getPermitAllPatterns();

    protected void configureSession(HttpSecurity http) throws Exception {
    }

    protected void configureLogin(HttpSecurity http) throws Exception {
    }

    protected void configureExceptionHandling(HttpSecurity http) throws Exception {
    }

    protected void configureHttpSecurity(HttpSecurity http) throws Exception {
        http
                .csrf().disable();

        configureSession(http);
        configureLogin(http);

        http
                .cors()
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers(getPermitAllPatterns()).permitAll()
                .antMatchers(getSwaggerPermitAllPatterns()).permitAll()
                .anyRequest().authenticated();

        configureExceptionHandling(http);
    }
}
