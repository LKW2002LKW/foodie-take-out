package com.foodie.dto.user;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserRegisterDTO implements Serializable {

    private String phone;      // 手机号
    private String code;       // 验证码
    private String password;   // 密码（可选）
    private String nickname;   // 昵称（可选）

}