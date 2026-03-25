package com.foodie.common.utils;

import lombok.extern.slf4j.Slf4j;
import java.util.Random;

/**
 * 短信工具类（简化版，实际使用阿里云短信服务）
 */
@Slf4j
public class SmsUtil {

    /**
     * 生成6位数字验证码
     */
    public static String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * 发送短信验证码（模拟）
     * 实际项目中需要对接阿里云短信服务
     */
    public static boolean sendSms(String phone, String code) {
        log.info("发送短信验证码：phone={}, code={}", phone, code);
        // TODO: 实际对接阿里云短信服务
        // 这里只是模拟发送成功
        return true;
    }
}
