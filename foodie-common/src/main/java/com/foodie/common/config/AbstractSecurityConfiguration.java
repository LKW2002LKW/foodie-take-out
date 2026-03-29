package com.foodie.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

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

    protected void configureCors(CorsConfiguration config) {
        config.setAllowedOriginPatterns(Collections.singletonList("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
    }

    protected void configureHttpSecurity(HttpSecurity http) throws Exception {
        http.csrf().disable();

        configureSession(http);
        configureLogin(http);

        http.cors()
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers(getPermitAllPatterns()).permitAll()
                .antMatchers(getSwaggerPermitAllPatterns()).permitAll()
                .anyRequest().authenticated();

        configureExceptionHandling(http);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        configureHttpSecurity(http);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        configureCors(config);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsFilter corsFilter() {
        return new CorsFilter(corsConfigurationSource());
    }
}
