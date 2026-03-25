package com.foodie.vo.platform;

/**
 * @Author: wanderer
 * @Date: 2026/1/15 13:35
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 管理员登录返回VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginVO implements Serializable {

    private Long id;
    private String username;
    private String name;
    private Integer roleType;
    private String token;
}
