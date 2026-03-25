package com.foodie.merchant.controller;

import com.foodie.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/merchant/common")
public class CommonController {
    // common endpoints (file upload, etc.)


    // 本地保存路径（可在 application.yml 修改）
    @Value("${upload.dir}")
    private String uploadDir;

    // 访问前端 URL 前缀
    @Value("${upload.urlPrefix}")
    private String urlPrefix;

    @PostMapping("/upload")
    public Result upload(MultipartFile file) throws IOException {
        log.info("文件上传: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            return Result.error("文件为空");
        }

        // 原始文件名
        String originalFilename = file.getOriginalFilename();

        // 后缀
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 生成新文件名
        String newFileName = UUID.randomUUID().toString() + extension;

        // 拼接完整路径
        File dest = new File(uploadDir + File.separator + newFileName);

        // 目录不存在则创建
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }

        // 保存文件
        file.transferTo(dest);

        // 构建前端可以访问的 URL
        String fileUrl = urlPrefix + newFileName;
        log.info("图片已保存，访问 URL: {}", fileUrl);

        log.info("uploadDir = {}", uploadDir);
        log.info("urlPrefix = {}", urlPrefix);


        // 返回 URL
        return Result.success(fileUrl);
    }
}

