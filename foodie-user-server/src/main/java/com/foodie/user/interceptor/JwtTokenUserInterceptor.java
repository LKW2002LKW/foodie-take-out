package com.foodie.user.interceptor;

import com.foodie.common.constant.JwtClaimsConstant;
import com.foodie.common.enumeration.UserType;
import com.foodie.common.properties.JwtProperties;
import com.foodie.common.web.AbstractJwtTokenInterceptor;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * JWT令牌校验拦截器（用户端）
 */
@Component
@Slf4j
public class JwtTokenUserInterceptor extends AbstractJwtTokenInterceptor {

    public JwtTokenUserInterceptor(JwtProperties jwtProperties, StringRedisTemplate redisTemplate) {
        super(jwtProperties, redisTemplate);
    }

    @Override
    protected String getTokenName(JwtProperties jwtProperties) {
        return jwtProperties.getUserTokenName();
    }

    @Override
    protected String getSecretKey(JwtProperties jwtProperties) {
        return jwtProperties.getUserSecretKey();
    }

    @Override
    protected UserType getUserType() {
        return UserType.USER;
    }

    @Override
    protected Long getUserId(Claims claims) {
        return getClaimAsLong(claims, JwtClaimsConstant.USER_ID);
    }

    @Override
    protected void fillRequestAttributes(HttpServletRequest request, Claims claims) {
        Long userId = getClaimAsLong(claims, JwtClaimsConstant.USER_ID);
        request.setAttribute("userId", userId);
    }
}
