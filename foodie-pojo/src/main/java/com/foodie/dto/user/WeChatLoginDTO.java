package com.foodie.dto.user;

import lombok.Data;
import java.io.Serializable;

/**
 * @Author: wanderer
 * @Date: 2026/1/11 11:44
 */

@Data
public class WeChatLoginDTO implements Serializable {

    private String code;       // 微信临时登录凭证
}