package com.foodie.platform.config;

import com.foodie.common.config.AbstractSecurityConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends AbstractSecurityConfiguration {

    @Override
    protected String[] getPermitAllPatterns() {
        return new String[]{
                "/platform/**",
                "/error"
        };
    }

    @Override
    protected void configureExceptionHandling(HttpSecurity http) throws Exception {
        http.exceptionHandling()
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
    }
}
