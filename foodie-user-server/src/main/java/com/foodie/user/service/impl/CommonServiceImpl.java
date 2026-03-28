package com.foodie.user.service.impl;

import com.foodie.common.service.UploadFacade;
import com.foodie.user.service.CommonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: wanderer
 * @Date: 2026/1/25 12:17
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CommonServiceImpl implements CommonService {

    private final UploadFacade uploadFacade;

    @Override
    public String upload(MultipartFile file) {
        String url = uploadFacade.upload(file);
        log.info("文件上传成功：{}", url);
        return url;
    }

}
