package com.foodie.user.controller;

import com.foodie.common.constant.MessageConstant;
import com.foodie.common.context.BaseContext;
import com.foodie.common.result.Result;
import com.foodie.dto.user.*;
import com.foodie.entity.User;
import com.foodie.user.service.UserService;
import com.foodie.vo.user.UserInfoVO;
import com.foodie.vo.user.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户认证
 */
@RestController
@RequestMapping("/user")
@Api(tags = "用户认证-个人中心")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;



    /**
     * 发送短信验证码
     */
    @PostMapping("/code")
    @ApiOperation("发送短信验证码")
    public Result<String> sendCode(@RequestParam String phone) {
        log.info("发送验证码：phone={}", phone);

        userService.sendCode(phone);
        return Result.success(MessageConstant.CODE_SEND_SUCCESS);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @ApiOperation("用户注册")
    public Result<UserLoginVO> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("用户注册：{}", userRegisterDTO);

        UserLoginVO userLoginVO = userService.register(userRegisterDTO);
        return Result.success(userLoginVO);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("用户登录：{}", userLoginDTO);

        UserLoginVO userLoginVO = userService.login(userLoginDTO);
        return Result.success(userLoginVO);
    }

    /**
     * 微信登录
     */
    @PostMapping("/wechat/login")
    @ApiOperation("微信登录")
    public Result<UserLoginVO> wechatLogin(@RequestBody WeChatLoginDTO weChatLoginDTO) {
        log.info("微信登录：{}", weChatLoginDTO);

        UserLoginVO userLoginVO = userService.wechatLogin(weChatLoginDTO);
        return Result.success(userLoginVO);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    @ApiOperation("退出登录")
    public Result<String> logout(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("用户退出登录：userId={}", userId);

        // 可以在这里清除Redis中的Token（可选）
        return Result.success("退出成功");
    }

    /**
     * 获取当前用户信息
     */
    //@GetMapping("/info")
    @ApiOperation("获取当前用户信息")
    public Result<UserLoginVO> getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("获取用户信息：userId={}", userId);

       User user = userService.getById(userId);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();

        return Result.success(userLoginVO);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/profile/info")
    @ApiOperation("获取用户信息")
    public Result<UserInfoVO> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("获取用户信息: userId={}", userId);

        UserInfoVO userInfo = userService.getUserInfo(userId);

        return Result.success(userInfo);
    }

    /**
     * 更新用户资料
     */
    @PutMapping("/profile/update")
    @ApiOperation("更新用户资料")
    public Result<Void> updateProfile(
            HttpServletRequest request,
            @Validated @RequestBody UserProfileUpdateDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("更新用户资料: userId={}, dto={}", userId, dto);

        userService.updateProfile(userId, dto);

        return Result.success();
    }

    /**
     * 修改密码
     */
    @PutMapping("/profile/password")
    @ApiOperation("修改密码")
    public Result<Void> updatePassword(
            HttpServletRequest request,
            @Validated @RequestBody PasswordUpdateDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("修改密码: userId={}", userId);

        userService.updatePassword(userId, dto);

        return Result.success();
    }

    /**
     * 绑定手机号
     */
    @PostMapping("/profile/bind-phone")
    @ApiOperation("绑定手机号")
    public Result<Void> bindPhone(
            HttpServletRequest request,
            @Validated @RequestBody PhoneBindDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("绑定手机号: userId={}, phone={}", userId, dto.getPhone());

        userService.bindPhone(userId, dto);

        return Result.success();
    }

    /**
     * 绑定 / 更换绑定微信
     */
    @PostMapping("/profile/wechat")
    public Result<Void> bindWechat(@RequestBody WechatBindDTO dto) {

        Long userId = BaseContext.getCurrentId();
        userService.bindWechat(userId, dto);

        return Result.success();
    }

    /**
     * 发送短信验证码
     */
    @PostMapping("/profile/sms-code")
    @ApiOperation("发送短信验证码")
    public Result<Void> sendSmsCode(
            @ApiParam("手机号") @RequestParam String phone) {

        log.info("发送短信验证码: phone={}", phone);

        userService.sendSmsCode(phone);

        return Result.success();
    }
}
