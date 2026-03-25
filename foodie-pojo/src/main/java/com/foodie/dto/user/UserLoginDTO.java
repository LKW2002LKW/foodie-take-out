package com.foodie.dto.user;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserLoginDTO implements Serializable {

    private String phone;      // 手机号
    private String code;       // 验证码
    private String password;   // 密码（密码登录时使用）
    private Integer loginType; // 登录类型：1-验证码登录 2-密码登录 3-微信登录
}