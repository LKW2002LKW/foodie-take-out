package com.foodie.user.service.impl;

import com.foodie.common.service.FileStorageService;
import com.foodie.user.service.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: wanderer
 * @Date: 2026/1/25 12:17
 */
@Service
@Slf4j
public class CommonServiceImpl implements CommonService {

    @Autowired
    private FileStorageService fileStorageService;

    @Override
    public String upload(MultipartFile file) {
        String url = fileStorageService.upload(file, "user");
        log.info("文件上传成功：{}", url);
        return url;
    }

}
