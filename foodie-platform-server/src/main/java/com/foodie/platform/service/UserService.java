package com.foodie.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodie.dto.platform.UserPageQueryDTO;
import com.foodie.entity.User;
import com.foodie.vo.platform.UserDetailVO;


/**
 * 用户服务接口（平台端）
 */
public interface UserService {

    /**
     * 用户分页查询
     */
    Page<User> pageQuery(UserPageQueryDTO userPageQueryDTO);

    /**
     * 用户详情
     */
    UserDetailVO getDetail(Long id);

    /**
     * 启用用户
     */
    void enableUser(Long id);

    /**
     * 禁用用户
     */
    void disableUser(Long id);
}