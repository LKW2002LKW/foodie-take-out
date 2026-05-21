package com.foodie.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.foodie.common.result.Result;
import com.foodie.dto.platform.UserPageQueryDTO;
import com.foodie.entity.User;
import com.foodie.platform.service.UserService;
import com.foodie.vo.platform.UserDetailVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/platform/user")
@Api(tags = "平台端-用户管理接口")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户分页查询
     */
    @GetMapping("/page")
    @ApiOperation("用户分页查询")
    public Result<Page<User>> page(UserPageQueryDTO userPageQueryDTO) {
        log.info("用户分页查询：{}", userPageQueryDTO);
        Page<User> page = userService.pageQuery(userPageQueryDTO);
        return Result.success(page);
    }

    /**
     * 用户详情
     */
    @GetMapping("/{id}")
    @ApiOperation("用户详情")
    public Result<UserDetailVO> getDetail(@PathVariable Long id) {
        log.info("查询用户详情：{}", id);
        UserDetailVO detailVO = userService.getDetail(id);
        return Result.success(detailVO);
    }

    /**
     * 启用用户
     */
    @PutMapping("/enable/{id}")
    @ApiOperation("启用用户")
    public Result<Void> enableUser(@PathVariable Long id) {
        log.info("启用用户：{}", id);
        userService.enableUser(id);
        return Result.success();
    }

    /**
     * 禁用用户
     */
    @PutMapping("/disable/{id}")
    @ApiOperation("禁用用户")
    public Result<Void> disableUser(@PathVariable Long id) {
        log.info("禁用用户：{}", id);
        userService.disableUser(id);
        return Result.success();
    }
}