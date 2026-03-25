package com.foodie.platform.interceptor;

import com.foodie.common.constant.JwtClaimsConstant;
import com.foodie.common.constant.MessageConstant;
import com.foodie.common.context.BaseContext;

import com.foodie.common.properties.JwtProperties;
import com.foodie.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT令牌校验拦截器（平台端）
 */
@Component
@Slf4j
public class JwtTokenPlatformInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();

        // ===== ① 接口文档 & 静态资源直接放行 =====
        if (uri.startsWith("/doc.html")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/webjars")) {
            return true;
        }

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader(jwtProperties.getPlatformTokenName());
        // 如果 token 还是 null，尝试 Authorization
        if (token == null || token.isEmpty()) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        log.info("JWT拦截器（平台端）：token={}", token);

        try {
            if (token == null || token.isEmpty()) {
                log.error("JWT令牌为空");
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":0,\"msg\":\"" + MessageConstant.USER_NOT_LOGIN + "\"}");
                return false;
            }

            Claims claims = JwtUtil.parseJWT(jwtProperties.getPlatformSecretKey(), token);
            Long adminId = Long.valueOf(claims.get(JwtClaimsConstant.ADMIN_ID).toString());
            String username = claims.get(JwtClaimsConstant.USERNAME).toString();

            log.info("当前平台管理员ID：{}, 用户名：{}", adminId, username);

            BaseContext.setCurrentId(adminId);
            request.setAttribute("adminId", adminId);
            request.setAttribute("username", username);


            return true;

        } catch (Exception ex) {
            log.error("JWT令牌校验失败：{}", ex.getMessage());
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":0,\"msg\":\"" + MessageConstant.TOKEN_INVALID + "\"}");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseContext.removeCurrentId();
    }
}