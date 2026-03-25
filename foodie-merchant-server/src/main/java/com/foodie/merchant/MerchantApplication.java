package com.foodie.merchant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = "com.foodie")
@EnableTransactionManagement
@Slf4j
public class MerchantApplication {

    public static void main(String[] args) {
        SpringApplication.run(MerchantApplication.class, args);
        log.info("商户管理端服务启动成功！访问地址：http://localhost:8082/doc.html");
    }
}