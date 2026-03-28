package com.foodie.common.config;

public class WebMvcModuleMetadata {

    private final String swaggerBasePackage;
    private final String swaggerTitle;
    private final String swaggerDescription;
    private final String[] allowedOrigins;

    public WebMvcModuleMetadata(String swaggerBasePackage,
                                String swaggerTitle,
                                String swaggerDescription,
                                String[] allowedOrigins) {
        this.swaggerBasePackage = swaggerBasePackage;
        this.swaggerTitle = swaggerTitle;
        this.swaggerDescription = swaggerDescription;
        this.allowedOrigins = allowedOrigins;
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
}
