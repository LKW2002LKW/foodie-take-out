package com.foodie.platform.service;


import com.foodie.dto.platform.PlatformAdminLoginDTO;
import com.foodie.vo.platform.AdminLoginVO;

/**
 * 管理员服务接口
 */
public interface PlatformAdminService {

    /**
     * 管理员登录
     * @param adminLoginDTO 登录信息
     * @return 登录结果
     */
    AdminLoginVO login(PlatformAdminLoginDTO adminLoginDTO);
}