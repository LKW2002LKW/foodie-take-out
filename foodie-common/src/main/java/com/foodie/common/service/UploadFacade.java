package com.foodie.common.service;

import com.foodie.common.properties.UploadModuleProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class UploadFacade {

    private final FileStorageService fileStorageService;
    private final UploadModuleProperties uploadModuleProperties;

    public String upload(MultipartFile file) {
        log.info("文件上传: {}", file.getOriginalFilename());
        return fileStorageService.upload(file, uploadModuleProperties.getServicePath());
    }
}
