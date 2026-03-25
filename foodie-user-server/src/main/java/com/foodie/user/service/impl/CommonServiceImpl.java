package com.foodie.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.foodie.common.result.Result;
import com.foodie.entity.Merchant;
import com.foodie.entity.OrderReview;
import com.foodie.user.mapper.OrderReviewMapper;
import com.foodie.user.service.CommonService;
import com.wechat.pay.java.service.payments.h5.model.QueryOrderByIdRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @Author: wanderer
 * @Date: 2026/1/25 12:17
 */
@Service
@Slf4j
public class CommonServiceImpl implements CommonService {

    @Value("${upload.dir}")
    private String uploadDir;

    @Value("${upload.urlPrefix}")
    private String urlPrefix;



    @Override
    public String upload( MultipartFile file) {



            if (file == null || file.isEmpty()) {
                throw new RuntimeException("文件为空");
            }

            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID() + suffix;

            File dest = new File(uploadDir, fileName);
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }

            try {
                file.transferTo(dest);
            } catch (IOException e) {
                throw new RuntimeException("文件上传失败", e);
            }

            String url = urlPrefix + fileName;
            log.info("文件上传成功：{}", url);
            return url;
        }

}


