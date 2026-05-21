package com.foodie.merchant.config;

import com.foodie.common.config.AbstractSecurityConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * @author wanderer
 * @since 2026-01-09
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends AbstractSecurityConfiguration {

    @Override
    protected String[] getPermitAllPatterns() {
        return new String[]{
                "/merchant/**"
        };
    }

    @Override
    protected void configureHttpSecurity(HttpSecurity http) throws Exception {
        http.csrf().disable();

        http.cors()
                .and()
                .authorizeRequests()
                .antMatchers(getPermitAllPatterns()).permitAll()
                .antMatchers(getSwaggerPermitAllPatterns()).permitAll()
                .anyRequest().authenticated();
    }
}
