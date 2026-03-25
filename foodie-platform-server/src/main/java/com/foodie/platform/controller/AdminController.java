package com.foodie.platform.controller;


import com.foodie.common.result.Result;
import com.foodie.dto.platform.PlatformAdminLoginDTO;
import com.foodie.platform.service.PlatformAdminService;
import com.foodie.vo.platform.AdminLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 平台管理员控制器
 */
@RestController
@RequestMapping("/platform/admin")
@Api(tags = "平台端-管理员接口")
@Slf4j
public class AdminController {

    @Autowired
    private PlatformAdminService adminService;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    @ApiOperation("管理员登录")
    public Result<AdminLoginVO> login(@RequestBody @Validated PlatformAdminLoginDTO adminLoginDTO) {
        log.info("管理员登录：{}", adminLoginDTO.getUsername());
        AdminLoginVO adminLoginVO = adminService.login(adminLoginDTO);
        return Result.success(adminLoginVO);
    }
}