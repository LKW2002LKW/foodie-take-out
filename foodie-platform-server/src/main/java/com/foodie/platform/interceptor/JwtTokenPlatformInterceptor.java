package com.foodie.platform.interceptor;

import com.foodie.common.constant.JwtClaimsConstant;
import com.foodie.common.enumeration.UserType;
import com.foodie.common.properties.JwtProperties;
import com.foodie.common.web.AbstractJwtTokenInterceptor;
import com.foodie.common.web.JwtInterceptorMetadata;
import io.jsonwebtoken.Claims;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * JWT令牌校验拦截器（平台端）
 */
@Component
public class JwtTokenPlatformInterceptor extends AbstractJwtTokenInterceptor {

    public JwtTokenPlatformInterceptor(JwtProperties jwtProperties, RedisTemplate<String, Object> redisTemplate) {
        super(jwtProperties, redisTemplate, new JwtInterceptorMetadata(
                JwtProperties::getPlatformTokenName,
                JwtProperties::getPlatformSecretKey,
                UserType.PLATFORMADMIN
        ));
    }

    @Override
    protected Long getUserId(Claims claims) {
        return getClaimAsLong(claims, JwtClaimsConstant.ADMIN_ID);
    }

    @Override
    protected void fillRequestAttributes(HttpServletRequest request, Claims claims) {
        Long adminId = getClaimAsLong(claims, JwtClaimsConstant.ADMIN_ID);
        String username = getClaimAsString(claims, JwtClaimsConstant.USERNAME);

        request.setAttribute("adminId", adminId);
        request.setAttribute("username", username);
    }
}
