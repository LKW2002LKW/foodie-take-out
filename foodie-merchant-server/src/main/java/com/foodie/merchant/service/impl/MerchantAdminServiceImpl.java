package com.foodie.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.foodie.common.constant.JwtClaimsConstant;
import com.foodie.common.constant.MessageConstant;
import com.foodie.common.constant.RedisKeyConstant;
import com.foodie.common.constant.ResultCodeConstant;
import com.foodie.common.constant.StatusConstant;
import com.foodie.common.enumeration.UserType;
import com.foodie.common.exception.AccountLockedException;
import com.foodie.common.exception.AccountNotFoundException;
import com.foodie.common.exception.BaseException;
import com.foodie.common.exception.PasswordErrorException;
import com.foodie.common.properties.JwtProperties;
import com.foodie.common.utils.JwtUtil;
import com.foodie.common.utils.MD5Util;
import com.foodie.dto.merchant.MerchantLoginDTO;
import com.foodie.dto.merchant.MerchantRegisterDTO;
import com.foodie.entity.Merchant;
import com.foodie.entity.MerchantAdmin;
import com.foodie.entity.SystemConfig;
import com.foodie.merchant.mapper.MerchantAdminMapper;
import com.foodie.merchant.mapper.MerchantMapper;
import com.foodie.merchant.service.MerchantAdminService;
import com.foodie.merchant.service.SystemConfigService;
import com.foodie.vo.merchant.MerchantLoginVO;
import com.foodie.vo.merchant.MerchantRegisterVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MerchantAdminServiceImpl implements MerchantAdminService {

    @Autowired
    private MerchantAdminMapper merchantAdminMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 商户注册
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantRegisterVO register(MerchantRegisterDTO merchantRegisterDTO) {
        log.info("商户注册：{}", merchantRegisterDTO);

        // 1. 校验商户名称是否已存在
        LambdaQueryWrapper<Merchant> merchantWrapper = new LambdaQueryWrapper<>();
        merchantWrapper.eq(Merchant::getMerchantName, merchantRegisterDTO.getMerchantName());
        if (merchantMapper.selectCount(merchantWrapper) > 0) {
            throw new BaseException(MessageConstant.MERCHANT_NAME_ALREADY_EXISTS);
        }

        // 2. 校验用户名是否已存在
        LambdaQueryWrapper<MerchantAdmin> adminWrapper = new LambdaQueryWrapper<>();
        adminWrapper.eq(MerchantAdmin::getUsername, merchantRegisterDTO.getUsername());
        if (merchantAdminMapper.selectCount(adminWrapper) > 0) {
            throw new BaseException(MessageConstant.USERNAME_ALREADY_EXISTS);
        }

        // 3. 校验手机号是否已存在
        LambdaQueryWrapper<Merchant> phoneWrapper = new LambdaQueryWrapper<>();
        phoneWrapper.eq(Merchant::getContactPhone, merchantRegisterDTO.getContactPhone());
        if (merchantMapper.selectCount(phoneWrapper) > 0) {
            throw new BaseException(MessageConstant.PHONE_ALREADY_EXISTS);
        }

        // 4. 创建商户
        Merchant merchant = new Merchant();
        BeanUtils.copyProperties(merchantRegisterDTO, merchant);

// 商户编码
        String merchantCode = "M" + (System.currentTimeMillis() % 1000000)
                + String.format("%04d", (int) (Math.random() * 10000));
        merchant.setMerchantCode(merchantCode);

// 状态
        merchant.setStatus(StatusConstant.MERCHANT_CLOSED);
        merchant.setAuditStatus(StatusConstant.AUDIT_PENDING);

// ===== 核心：系统默认值注入 =====

        merchant.setDeliveryFee(
                merchant.getDeliveryFee() != null
                        ? merchant.getDeliveryFee()
                        : systemConfigService.getDecimal("default_delivery_fee")
        );

        merchant.setMinDeliveryAmount(
                merchant.getMinDeliveryAmount() != null
                        ? merchant.getMinDeliveryAmount()
                        : systemConfigService.getDecimal("min_delivery_amount")
        );

        merchant.setCommissionRate(
                systemConfigService.getDecimal("default_commission_rate")
        );


        merchantMapper.insert(merchant);


        // 5. 创建商户管理员账号
        MerchantAdmin merchantAdmin = new MerchantAdmin();
        merchantAdmin.setMerchantId(merchant.getId());
        merchantAdmin.setUsername(merchantRegisterDTO.getUsername());
        merchantAdmin.setPassword(MD5Util.encrypt(merchantRegisterDTO.getPassword()));
        merchantAdmin.setName(merchantRegisterDTO.getName());
        merchantAdmin.setPhone(merchantRegisterDTO.getPhone());

        merchantAdmin.setStatus(StatusConstant.ENABLE);

        merchantAdminMapper.insert(merchantAdmin);

        // 6. 返回结果
        return MerchantRegisterVO.builder()
                .merchantId(merchant.getId())
                .merchantCode(merchantCode)
                .message("注册成功，请等待平台审核")
                .build();
    }

    /**
     * 商户登录
     */
    @Override
    public MerchantLoginVO login(MerchantLoginDTO merchantLoginDTO) {
        log.info("商户登录：{}", merchantLoginDTO.getUsername());

        String username = merchantLoginDTO.getUsername();
        String password = merchantLoginDTO.getPassword();

        // 1. 根据用户名查询管理员
        LambdaQueryWrapper<MerchantAdmin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantAdmin::getUsername, username);
        MerchantAdmin merchantAdmin = merchantAdminMapper.selectOne(wrapper);

        if (merchantAdmin == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 2. 校验密码
        String md5Password = MD5Util.encrypt(password);
        if (!md5Password.equals(merchantAdmin.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        // 3. 校验账号状态
        if (merchantAdmin.getStatus().equals(StatusConstant.DISABLE)) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        // 4. 查询商户信息
        Merchant merchant = merchantMapper.selectById(merchantAdmin.getMerchantId());
        if (merchant == null) {
            throw new BaseException(MessageConstant.MERCHANT_NOT_FOUND);
        }

        // 5. 校验商户审核状态
        if (!merchant.getAuditStatus().equals(StatusConstant.AUDIT_PASSED)) {
            throw new BaseException(
                    ResultCodeConstant.MERCHANT_NOT_APPROVED,
                    MessageConstant.MERCHANT_NOT_APPROVED);
        }

        // 6. 生成JWT令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.ADMIN_ID, merchantAdmin.getId());
        claims.put(JwtClaimsConstant.MERCHANT_ID, merchant.getId());
        claims.put(JwtClaimsConstant.USERNAME, username);
        claims.put(JwtClaimsConstant.NAME, merchantAdmin.getName());

        String token = JwtUtil.createJWT(
                jwtProperties.getMerchantSecretKey(),
                jwtProperties.getMerchantTtl(),
                claims
        );

        String tokenKey = String.format(RedisKeyConstant.TOKEN, UserType.MERCHANT.name(), merchantAdmin.getId());
        try {
            redisTemplate.opsForValue().set(tokenKey, token, jwtProperties.getMerchantTtl(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("写入商户token缓存失败：adminId={}", merchantAdmin.getId(), e);
        }

        // 7. 返回结果
        return MerchantLoginVO.builder()
                .id(merchantAdmin.getId())
                .username(username)
                .name(merchantAdmin.getName())
                .token(token)
                .merchantId(merchant.getId())
                .merchantName(merchant.getMerchantName())
                .build();
    }
}