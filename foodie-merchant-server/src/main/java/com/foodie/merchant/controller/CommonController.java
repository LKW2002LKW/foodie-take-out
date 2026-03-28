package com.foodie.merchant.controller;

import com.foodie.common.result.Result;
import com.foodie.common.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/merchant/common")
@RequiredArgsConstructor
public class CommonController {
    // common endpoints (file upload, etc.)

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public Result upload(MultipartFile file) {
        log.info("文件上传: {}", file.getOriginalFilename());
        String fileUrl = fileStorageService.upload(file, "merchant");
        return Result.success(fileUrl);
    }
}
