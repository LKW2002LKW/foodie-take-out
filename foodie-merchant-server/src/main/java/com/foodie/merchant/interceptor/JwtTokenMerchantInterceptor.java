package com.foodie.merchant.interceptor;

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
 * JWT令牌校验拦截器（商户端）
 */
@Component
public class JwtTokenMerchantInterceptor extends AbstractJwtTokenInterceptor {

    public JwtTokenMerchantInterceptor(JwtProperties jwtProperties, RedisTemplate<String, Object> redisTemplate) {
        super(jwtProperties, redisTemplate, new JwtInterceptorMetadata(
                JwtProperties::getMerchantTokenName,
                JwtProperties::getMerchantSecretKey,
                UserType.MERCHANT
        ));
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
}
