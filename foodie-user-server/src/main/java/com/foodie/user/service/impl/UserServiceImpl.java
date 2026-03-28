package com.foodie.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodie.common.constant.*;
import com.foodie.common.enumeration.UserType;
import com.foodie.common.exception.BaseException;
import com.foodie.common.exception.BusinessException;
import com.foodie.common.properties.JwtProperties;
import com.foodie.common.utils.JwtUtil;
import com.foodie.common.utils.MD5Util;
import com.foodie.common.utils.SmsUtil;
import com.foodie.dto.user.*;
import com.foodie.entity.User;
import com.foodie.user.mapper.UserMapper;
import com.foodie.user.service.UserService;
import com.foodie.vo.user.UserInfoVO;
import com.foodie.vo.user.UserLoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final StringRedisTemplate redisTemplate;

    private final JwtProperties jwtProperties;

    private final UserMapper userMapper;

    /**
     * 发送短信验证码
     */
    @Override
    public void sendCode(String phone) {
        log.info("发送验证码：phone={}", phone);

        // 生成验证码
        String code = SmsUtil.generateCode();

        // 发送短信
        boolean success = SmsUtil.sendSms(phone, code);
        if (!success) {
            throw new BaseException("验证码发送失败");
        }

        // 将验证码存入Redis（5分钟过期）
        String key = String.format(RedisKeyConstant.SMS_CODE, phone);
        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);

        log.info("验证码已发送：phone={}, code={}", phone, code);
    }

    /**
     * 用户注册
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserLoginVO register(UserRegisterDTO userRegisterDTO) {
        log.info("用户注册：{}", userRegisterDTO);

        String phone = userRegisterDTO.getPhone();
        String code = userRegisterDTO.getCode();

        // 1. 校验验证码
        validateCode(phone, code);

        // 2. 检查手机号是否已注册
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);

        if (this.count(wrapper) > 0) {
            throw new BaseException(MessageConstant.PHONE_ALREADY_REGISTERED);
        }

        // 3. 创建用户
        User user = new User();
        user.setPhone(phone);
        // 3. 手机号注册 → 一定绑定手机
        user.setHasPhone(true);
        user.setNickname(StringUtils.hasText(userRegisterDTO.getNickname())
                ? userRegisterDTO.getNickname()
                : "用户" + phone.substring(7)); // 默认昵称：用户+后4位手机号

        // 如果提供了密码，则加密存储
        if (StringUtils.hasText(userRegisterDTO.getPassword())) {
            user.setPassword(MD5Util.encrypt(userRegisterDTO.getPassword()));
            user.setHasPassword(true);
        }else {
            user.setHasPassword(false);
        }
        //是否绑定微信
        user.setHasWechat(false);

        user.setStatus(StatusConstant.ENABLE);
        this.save(user);

        // 4. 生成JWT令牌
        String token = generateToken(user);

        // 5. 删除Redis中的验证码
        String key = String.format(RedisKeyConstant.SMS_CODE, phone);
        redisTemplate.delete(key);

        // 6. 返回结果
        return UserLoginVO.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .token(token)
                .build();
    }

    /**
     * 用户登录
     */
    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        log.info("用户登录：{}", userLoginDTO);

        String phone = userLoginDTO.getPhone();
        Integer loginType = userLoginDTO.getLoginType();

        User user = null;

        // 根据登录类型处理
        if (loginType == 1) {
            // 验证码登录
            String code = userLoginDTO.getCode();
            validateCode(phone, code);

            // 查询用户（如果不存在则自动注册）
            user = getUserByPhone(phone);
            if (user == null) {
                user = autoRegister(phone);
            }

            // 删除验证码
            String key = String.format(RedisKeyConstant.SMS_CODE, phone);
            redisTemplate.delete(key);

        } else if (loginType == 2) {
            // 密码登录
            String password = userLoginDTO.getPassword();

            // 查询用户
            user = getUserByPhone(phone);
            if (user == null) {
                throw new BaseException(MessageConstant.PHONE_NOT_REGISTERED);
            }

            // 校验密码
            String md5Password = MD5Util.encrypt(password);
            if (!md5Password.equals(user.getPassword())) {
                throw new BaseException(MessageConstant.PASSWORD_ERROR);
            }
        }

        // 检查账号状态
        if (user.getStatus().equals(StatusConstant.DISABLE)) {
            throw new BaseException(MessageConstant.ACCOUNT_LOCKED);
        }

        // 生成JWT令牌
        String token = generateToken(user);

        // 返回结果
        return UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .token(token)
                .build();
    }

    /**
     * 微信登录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserLoginVO wechatLogin(WeChatLoginDTO weChatLoginDTO) {
        log.info("微信登录：{}", weChatLoginDTO);

        // 1. 调用微信接口获取openid
        // TODO: 实际项目中需要对接微信登录API
        String openid = getOpenidFromWeChat(weChatLoginDTO.getCode());
        if (openid == null) {
            throw new BaseException(MessageConstant.WECHAT_LOGIN_FAILED);
        }

        // 2. 查询用户（根据openid）
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User user = this.getOne(wrapper);

        // 3. 如果用户不存在，自动注册
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setHasPhone(false);
            user.setHasPassword(false);
            user.setHasWechat(true);
            user.setNickname("微信用户" + System.currentTimeMillis() % 10000);
            user.setStatus(StatusConstant.ENABLE);
            this.save(user);
        }

        // 4. 检查账号状态
        if (user.getStatus().equals(StatusConstant.DISABLE)) {
            throw new BaseException(MessageConstant.ACCOUNT_LOCKED);
        }

        // 5. 生成JWT令牌
        String token = generateToken(user);

        // 6. 返回结果
        return UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .token(token)
                .build();
    }

    /**
     * 校验验证码
     */
    private void validateCode(String phone, String code) {
        String key = String.format(RedisKeyConstant.SMS_CODE, phone);
        String cacheCode = redisTemplate.opsForValue().get(key);

        if (cacheCode == null) {
            throw new BaseException(MessageConstant.CODE_EXPIRED);
        }

        if (!cacheCode.equals(code)) {
            throw new BaseException(MessageConstant.CODE_ERROR);
        }
    }

    /**
     * 根据手机号查询用户
     */
    private User getUserByPhone(String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        return this.getOne(wrapper);
    }

    /**
     * 自动注册
     */
    private User autoRegister(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickname("用户" + phone.substring(7));
        user.setStatus(StatusConstant.ENABLE);
        this.save(user);
        return user;
    }

    /**
     * 生成JWT令牌
     */
    private String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("phone", user.getPhone());

        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims
        );

        String tokenKey = String.format(RedisKeyConstant.TOKEN, UserType.USER.name(), user.getId());
        try {
            redisTemplate.opsForValue().set(tokenKey, token, jwtProperties.getUserTtl(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("写入用户token缓存失败：userId={}", user.getId(), e);
        }

        return token;
    }

    /**
     * 从微信获取openid（模拟）
     */
    private String getOpenidFromWeChat(String code) {
        // TODO: 实际对接微信登录API
        // 使用code换取openid和session_key
        log.info("调用微信API获取openid：code={}", code);

        // 这里只是模拟返回
        return "mock_openid_" + System.currentTimeMillis();
    }


    @Override
    public UserInfoVO getUserInfo(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 转换为VO
        UserInfoVO vo = new UserInfoVO();
        BeanUtils.copyProperties(user, vo);

        // 设置性别名称
        vo.setSexName(UserConstant.getSexName(user.getSex()));

        // 设置是否绑定手机
        vo.setHasPhone(StringUtils.hasText(user.getPhone()));

        // 设置是否设置密码
        vo.setHasPassword(StringUtils.hasText(user.getPassword()));

        vo.setHasWechat(StringUtils.hasText(user.getOpenid()));

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, UserProfileUpdateDTO dto) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新信息
        user.setNickname(dto.getNickname());
        user.setAvatar(dto.getAvatar());
        user.setSex(dto.getSex());
        user.setBirthday(dto.getBirthday());

        // 保存
        int rows = userMapper.updateById(user);
        if (rows == 0) {
            throw new BusinessException("更新失败");
        }

        log.info("用户资料更新成功: userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, PasswordUpdateDTO dto) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        // 验证两次密码是否一致
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次密码输入不一致");
        }

        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证旧密码
        String oldPasswordMd5 = MD5Util.encrypt(dto.getOldPassword());
        if (!oldPasswordMd5.equals(user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        // 新密码不能与旧密码相同
        if (dto.getOldPassword().equals(dto.getNewPassword())) {
            throw new BusinessException("新密码不能与原密码相同");
        }

        // 更新密码
        String newPasswordMd5 = MD5Util.encrypt(dto.getNewPassword());
        user.setPassword(newPasswordMd5);

        // 保存
        int rows = userMapper.updateById(user);
        if (rows == 0) {
            throw new BusinessException("修改密码失败");
        }

        log.info("用户密码修改成功: userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindPhone(Long userId, PhoneBindDTO dto) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        // 验证验证码
        String codeKey = UserConstant.SMS_CODE_PREFIX + dto.getPhone();
        String cachedCode = redisTemplate.opsForValue().get(codeKey);

        if (!StringUtils.hasText(cachedCode)) {
            throw new BusinessException("验证码已过期，请重新获取");
        }

        if (!cachedCode.equals(dto.getCode())) {
            throw new BusinessException("验证码错误");
        }

        // 检查手机号是否已被其他用户绑定
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, dto.getPhone());
        queryWrapper.ne(User::getId, userId);
        Long count = userMapper.selectCount(queryWrapper);

        if (count > 0) {
            throw new BusinessException("该手机号已被其他账号绑定");
        }

        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新手机号
        user.setPhone(dto.getPhone());

        // 如果提供了密码，同时设置密码
        if (StringUtils.hasText(dto.getPassword())) {
            String passwordMd5 = MD5Util.encrypt(dto.getPassword());
            user.setPassword(passwordMd5);
        }

        // 保存
        int rows = userMapper.updateById(user);
        if (rows == 0) {
            throw new BusinessException("绑定手机号失败");
        }

        // 删除验证码
        redisTemplate.delete(codeKey);

        log.info("用户绑定手机号成功: userId={}, phone={}", userId, dto.getPhone());
    }

    @Override
    public void sendSmsCode(String phone) {
        if (!StringUtils.hasText(phone)) {
            throw new BusinessException("手机号不能为空");
        }

        // 验证手机号格式
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("手机号格式不正确");
        }

        // 生成6位验证码
        String code = String.format("%06d", new Random().nextInt(1000000));

        // 存储到Redis，有效期5分钟
        String codeKey = UserConstant.SMS_CODE_PREFIX + phone;
        redisTemplate.opsForValue().set(
                codeKey,
                code,
                UserConstant.SMS_CODE_EXPIRE_MINUTES,
                TimeUnit.MINUTES
        );

        // TODO: 调用短信服务发送验证码
        // smsService.send(phone, code);

        log.info("验证码发送成功: phone={}, code={}", phone, code);
        // 开发环境打印验证码，生产环境删除此行
        System.out.println("【测试】验证码：" + code);
    }

    /**
     * 绑定微信/更换
     * @param userId
     * @param dto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindWechat(Long userId, WechatBindDTO dto) {

        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        if (!StringUtils.hasText(dto.getOpenid())) {
            throw new BusinessException("微信openid不能为空");
        }

        // 1. 查询当前用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 检查该 openid 是否已被其他用户绑定
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getOpenid, dto.getOpenid());
        queryWrapper.ne(User::getId, userId);

        Long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException("该微信已绑定其他账号");
        }

        // 3. 绑定 / 更换绑定微信
        user.setOpenid(dto.getOpenid());
        user.setHasWechat(true);

        int rows = userMapper.updateById(user);
        if (rows == 0) {
            throw new BusinessException("绑定微信失败");
        }

        log.info("用户绑定/更换微信成功: userId={}, openid={}", userId, dto.getOpenid());
    }

}
