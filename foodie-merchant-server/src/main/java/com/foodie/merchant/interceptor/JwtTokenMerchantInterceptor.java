package com.foodie.merchant.interceptor;

import com.foodie.common.constant.JwtClaimsConstant;
import com.foodie.common.enumeration.UserType;
import com.foodie.common.properties.JwtProperties;
import com.foodie.common.web.AbstractJwtTokenInterceptor;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * JWT令牌校验拦截器（商户端）
 */
@Component
@Slf4j
public class JwtTokenMerchantInterceptor extends AbstractJwtTokenInterceptor {

    public JwtTokenMerchantInterceptor(JwtProperties jwtProperties, RedisTemplate<String, Object> redisTemplate) {
        super(jwtProperties, redisTemplate);
    }

    @Override
    protected String getTokenName(JwtProperties jwtProperties) {
        return jwtProperties.getMerchantTokenName();
    }

    @Override
    protected String getSecretKey(JwtProperties jwtProperties) {
        return jwtProperties.getMerchantSecretKey();
    }

    @Override
    protected UserType getUserType() {
        return UserType.MERCHANT;
    }

    @Override
    protected Long getUserId(Claims claims) {
        return getClaimAsLong(claims, JwtClaimsConstant.ADMIN_ID);
    }

    @Override
    protected void fillRequestAttributes(HttpServletRequest request, Claims claims) {
        Long adminId = getClaimAsLong(claims, JwtClaimsConstant.ADMIN_ID);
        Long merchantId = getClaimAsLong(claims, JwtClaimsConstant.MERCHANT_ID);
        String username = getClaimAsString(claims, JwtClaimsConstant.USERNAME);

        request.setAttribute("merchantId", merchantId);
        request.setAttribute("adminId", adminId);
        request.setAttribute("username", username);
    }

    @Override
    protected boolean allowOptions() {
        return true;
    }
}
