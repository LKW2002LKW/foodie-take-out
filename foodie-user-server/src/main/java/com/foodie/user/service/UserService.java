package com.foodie.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.foodie.dto.user.*;
import com.foodie.entity.User;
import com.foodie.vo.user.UserInfoVO;
import com.foodie.vo.user.UserLoginVO;

public interface UserService extends IService<User> {

    /**
     * 发送短信验证码
     */
    void sendCode(String phone);

    /**
     * 用户注册
     */
    UserLoginVO register(UserRegisterDTO userRegisterDTO);

    /**
     * 用户登录
     */
    UserLoginVO login(UserLoginDTO userLoginDTO);

    /**
     * 微信登录
     */
    UserLoginVO wechatLogin(WeChatLoginDTO weChatLoginDTO);


    /**
     * 获取用户信息
     */
    UserInfoVO getUserInfo(Long userId);

    /**
     * 更新用户资料
     */
    void updateProfile(Long userId, UserProfileUpdateDTO dto);

    /**
     * 修改密码
     */
    void updatePassword(Long userId, PasswordUpdateDTO dto);

    /**
     * 绑定手机号
     */
    void bindPhone(Long userId, PhoneBindDTO dto);

    /**
     * 发送短信验证码
     */
    void sendSmsCode(String phone);


    void bindWechat(Long userId, WechatBindDTO dto);
}