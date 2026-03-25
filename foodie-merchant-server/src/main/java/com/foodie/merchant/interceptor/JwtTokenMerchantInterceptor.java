package com.foodie.merchant.interceptor;

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
 * JWT令牌校验拦截器（商户端）
 */
@Component
@Slf4j
public class JwtTokenMerchantInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 前置处理：校验JWT令牌
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {



        // Allow preflight OPTIONS requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String uri = request.getRequestURI();

        // ===== ① 接口文档 & 静态资源直接放行 =====
        if (uri.startsWith("/doc.html")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/webjars")) {
            return true;
        }

        // 判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            // 不是动态方法，直接放行
            return true;
        }

        // 1. 从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getMerchantTokenName());
        // 如果 token 还是 null，尝试 Authorization
        if (token == null || token.isEmpty()) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        log.info("JWT拦截器：token={}", token);

        // 2. 校验令牌
        try {
            if (token == null || token.isEmpty()) {
                log.error("JWT令牌为空");
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":0,\"msg\":\"" + MessageConstant.USER_NOT_LOGIN + "\"}");
                return false;
            }

            // 3. 解析令牌
            Claims claims = JwtUtil.parseJWT(jwtProperties.getMerchantSecretKey(), token);
            Long adminId = Long.valueOf(claims.get(JwtClaimsConstant.ADMIN_ID).toString());
            Long merchantId = Long.valueOf(claims.get(JwtClaimsConstant.MERCHANT_ID).toString());
            String username = claims.get(JwtClaimsConstant.USERNAME).toString();

            log.info("当前商户管理员ID：{}, 商户ID：{}, 用户名：{}", adminId, merchantId, username);

            // 4. 将管理员ID存入ThreadLocal
            BaseContext.setCurrentId(adminId);

            // 5. 将商户ID存入请求属性（供Controller使用）
            request.setAttribute("merchantId", merchantId);
            request.setAttribute("adminId", adminId);
            request.setAttribute("username", username);

            // 6. 放行
            return true;

        } catch (Exception ex) {
            log.error("JWT令牌校验失败：{}", ex.getMessage());
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":0,\"msg\":\"" + MessageConstant.TOKEN_INVALID + "\"}");
            return false;
        }
    }

    /**
     * 后置处理：清理ThreadLocal
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseContext.removeCurrentId();
    }
}