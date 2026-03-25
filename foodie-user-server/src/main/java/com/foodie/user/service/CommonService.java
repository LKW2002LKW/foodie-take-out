package com.foodie.user.service;

import org.springframework.web.multipart.MultipartFile;

/**
* @Author: wanderer
* @Date: 2026/1/25 12:16 
*/public interface CommonService {
    String upload(MultipartFile file);
}
