package com.foodie.user.controller;

import com.foodie.common.result.Result;
import com.foodie.user.service.CommonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/user/common")
@Slf4j
@RequiredArgsConstructor
public class CommonController {

    private final CommonService uploadService;

    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) {
        String url = uploadService.upload(file);
        return Result.success(url);
    }
}

