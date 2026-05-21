package com.foodie.common.web;

import com.foodie.common.enumeration.UserType;
import com.foodie.common.properties.JwtProperties;

import java.util.function.Function;

public class JwtInterceptorMetadata {

    private final Function<JwtProperties, String> tokenNameResolver;
    private final Function<JwtProperties, String> secretKeyResolver;
    private final UserType userType;

    public JwtInterceptorMetadata(Function<JwtProperties, String> tokenNameResolver,
                                  Function<JwtProperties, String> secretKeyResolver,
                                  UserType userType) {
        this.tokenNameResolver = tokenNameResolver;
        this.secretKeyResolver = secretKeyResolver;
        this.userType = userType;
    }

    public String resolveTokenName(JwtProperties jwtProperties) {
        return tokenNameResolver.apply(jwtProperties);
    }

    public String resolveSecretKey(JwtProperties jwtProperties) {
        return secretKeyResolver.apply(jwtProperties);
    }

    public UserType getUserType() {
        return userType;
    }
}
