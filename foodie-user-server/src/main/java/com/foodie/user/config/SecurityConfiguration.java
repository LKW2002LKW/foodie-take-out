package com.foodie.user.config;

import com.foodie.common.config.AbstractSecurityConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

/**
 * Security / OAuth2 configuration skeleton
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends AbstractSecurityConfiguration {

    @Override
    protected String[] getPermitAllPatterns() {
        return new String[]{
                "/user/**"
        };
    }

    @Override
    protected void configureSession(HttpSecurity http) throws Exception {
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and();
    }

    @Override
    protected void configureLogin(HttpSecurity http) throws Exception {
        http.formLogin().disable()
                .httpBasic().disable();
    }
}
