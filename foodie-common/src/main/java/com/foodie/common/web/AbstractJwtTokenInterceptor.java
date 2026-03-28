package com.foodie.common.web;

import com.foodie.common.constant.MessageConstant;
import com.foodie.common.constant.RedisKeyConstant;
import com.foodie.common.context.BaseContext;
import com.foodie.common.enumeration.UserType;
import com.foodie.common.properties.JwtProperties;
import com.foodie.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractJwtTokenInterceptor implements HandlerInterceptor {

    private final JwtProperties jwtProperties;
    private final RedisTemplate<String, ?> redisTemplate;

    protected abstract String getTokenName(JwtProperties jwtProperties);

    protected abstract String getSecretKey(JwtProperties jwtProperties);

    protected abstract UserType getUserType();

    protected abstract Long getUserId(Claims claims);

    protected abstract void fillRequestAttributes(HttpServletRequest request, Claims claims);

    protected boolean allowOptions() {
        return true;
    }

    protected boolean allowSwaggerResources(String uri) {
        return uri.startsWith("/doc.html")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/webjars");
    }

    protected String resolveToken(HttpServletRequest request) {
        String token = request.getHeader(getTokenName(jwtProperties));
        if (token == null || token.isEmpty()) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }
        return token;
    }

    protected String resolveTokenKey(Claims claims) {
        Long userId = getUserId(claims);
        return String.format(RedisKeyConstant.TOKEN, getUserType().name(), userId);
    }

    protected void onTokenValidated(Claims claims) {
        Long userId = getUserId(claims);
        BaseContext.setCurrentId(userId);
    }

    protected void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":0,\"msg\":\"" + message + "\"}");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (allowOptions() && "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        if (allowSwaggerResources(uri)) {
            return true;
        }

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = resolveToken(request);
        log.info("JWT拦截器：token={}", token);

        try {
            if (token == null || token.isEmpty()) {
                log.error("JWT令牌为空");
                writeUnauthorized(response, MessageConstant.USER_NOT_LOGIN);
                return false;
            }

            Claims claims = JwtUtil.parseJWT(getSecretKey(jwtProperties), token);
            String tokenKey = resolveTokenKey(claims);
            Object cachedToken = redisTemplate.opsForValue().get(tokenKey);
            if (cachedToken == null || !token.equals(cachedToken.toString())) {
                log.error("JWT令牌已失效或被替换");
                writeUnauthorized(response, MessageConstant.TOKEN_INVALID);
                return false;
            }

            onTokenValidated(claims);
            fillRequestAttributes(request, claims);
            return true;

        } catch (Exception ex) {
            log.error("JWT令牌校验失败：{}", ex.getMessage());
            writeUnauthorized(response, MessageConstant.TOKEN_INVALID);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.removeCurrentId();
    }
}
