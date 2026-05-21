package com.foodie.common.config;

import org.springframework.web.servlet.HandlerInterceptor;

public class WebMvcModuleMetadata {

    private final String swaggerBasePackage;
    private final String swaggerTitle;
    private final String swaggerDescription;
    private final String[] allowedOrigins;
    private final HandlerInterceptor jwtInterceptor;
    private final String[] interceptorIncludePatterns;
    private final String[] interceptorExcludePatterns;

    public WebMvcModuleMetadata(String swaggerBasePackage,
                                String swaggerTitle,
                                String swaggerDescription,
                                String[] allowedOrigins,
                                HandlerInterceptor jwtInterceptor,
                                String[] interceptorIncludePatterns,
                                String[] interceptorExcludePatterns) {
        this.swaggerBasePackage = swaggerBasePackage;
        this.swaggerTitle = swaggerTitle;
        this.swaggerDescription = swaggerDescription;
        this.allowedOrigins = allowedOrigins;
        this.jwtInterceptor = jwtInterceptor;
        this.interceptorIncludePatterns = interceptorIncludePatterns;
        this.interceptorExcludePatterns = interceptorExcludePatterns;
    }

    public String getSwaggerBasePackage() {
        return swaggerBasePackage;
    }

    public String getSwaggerTitle() {
        return swaggerTitle;
    }

    public String getSwaggerDescription() {
        return swaggerDescription;
    }

    public String[] getAllowedOrigins() {
        return allowedOrigins;
    }

    public HandlerInterceptor getJwtInterceptor() {
        return jwtInterceptor;
    }

    public String[] getInterceptorIncludePatterns() {
        return interceptorIncludePatterns;
    }

    public String[] getInterceptorExcludePatterns() {
        return interceptorExcludePatterns;
    }
}
