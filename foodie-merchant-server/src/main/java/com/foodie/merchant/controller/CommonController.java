package com.foodie.merchant.controller;

import com.foodie.common.result.Result;
import com.foodie.common.service.UploadFacade;
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

    private final UploadFacade uploadFacade;

    @PostMapping("/upload")
    public Result upload(MultipartFile file) {
        String fileUrl = uploadFacade.upload(file);
        return Result.success(fileUrl);
    }
}
