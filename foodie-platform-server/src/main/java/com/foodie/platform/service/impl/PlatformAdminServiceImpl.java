package com.foodie.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.foodie.common.constant.JwtClaimsConstant;
import com.foodie.common.constant.MessageConstant;
import com.foodie.common.constant.RedisKeyConstant;
import com.foodie.common.enumeration.UserType;

import com.foodie.common.exception.AccountLockedException;
import com.foodie.common.exception.AccountNotFoundException;
import com.foodie.common.exception.PasswordErrorException;
import com.foodie.common.properties.JwtProperties;
import com.foodie.common.utils.JwtUtil;
import com.foodie.common.utils.MD5Util;

import com.foodie.dto.platform.PlatformAdminLoginDTO;
import com.foodie.entity.PlatformAdmin;
import com.foodie.platform.mapper.PlatformAdminMapper;

import com.foodie.platform.service.PlatformAdminService;
import com.foodie.vo.platform.AdminLoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 管理员服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PlatformAdminServiceImpl implements PlatformAdminService {

    private final PlatformAdminMapper platformAdminMapper;

    private final JwtProperties jwtProperties;

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 管理员登录
     */
    @Override
    public AdminLoginVO login(PlatformAdminLoginDTO adminLoginDTO) {
        String username = adminLoginDTO.getUsername();
        String password = adminLoginDTO.getPassword();

        // 1. 查询管理员
        LambdaQueryWrapper<PlatformAdmin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PlatformAdmin::getUsername, username);
        PlatformAdmin admin = platformAdminMapper.selectOne(queryWrapper);

        // 2. 校验
        if (admin == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 密码MD5加密后比对
        password = MD5Util.encrypt(password);
        if (!password.equals(admin.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        // 账号状态
        if (admin.getStatus() == 0) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        // 3. 更新最后登录时间
        LambdaUpdateWrapper<PlatformAdmin> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PlatformAdmin::getId, admin.getId())
                .set(PlatformAdmin::getLastLoginTime, LocalDateTime.now());
        platformAdminMapper.update(null, updateWrapper);

        // 4. 生成JWT令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.ADMIN_ID, admin.getId());
        claims.put(JwtClaimsConstant.USERNAME, admin.getUsername());
        claims.put(JwtClaimsConstant.NAME, admin.getName());
        claims.put(JwtClaimsConstant.ROLE_TYPE, admin.getRoleType());
        String token = JwtUtil.createJWT(jwtProperties.getPlatformSecretKey(),jwtProperties.getPlatformTtl(),claims);

        String tokenKey = String.format(RedisKeyConstant.TOKEN, UserType. PLATFORMADMIN.name(), admin.getId());
        try {
            redisTemplate.opsForValue().set(tokenKey, token, jwtProperties.getPlatformTtl(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("写入平台token缓存失败：adminId={}", admin.getId(), e);
        }

        // 5. 返回登录信息
        return AdminLoginVO.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .name(admin.getName())
                .roleType(admin.getRoleType())
                .token(token)
                .build();
    }
}