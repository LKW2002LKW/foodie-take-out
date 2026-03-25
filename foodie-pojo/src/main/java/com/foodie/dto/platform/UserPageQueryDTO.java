package com.foodie.dto.platform;

import lombok.Data;
import java.io.Serializable;

/**
 * 用户分页查询DTO
 */
@Data
public class UserPageQueryDTO implements Serializable {

    private Integer page = 1;
    private Integer pageSize = 10;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 账号状态 1正常 0禁用
     */
    private Integer status;
}