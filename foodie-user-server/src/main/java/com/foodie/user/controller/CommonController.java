package com.foodie.user.controller;

import com.foodie.common.result.Result;
import com.foodie.common.service.UploadFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user/common")
@Slf4j
@RequiredArgsConstructor
public class CommonController {

    private final UploadFacade uploadFacade;

    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) {
        String url = uploadFacade.upload(file);
        return Result.success(url);
    }
}

