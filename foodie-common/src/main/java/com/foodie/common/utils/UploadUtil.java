package com.foodie.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
public class UploadUtil {

    /**
     * 上传文件并返回访问 URL
     */
    public static String upload(
            MultipartFile file,
            String uploadDir,
            String urlPrefix
    ) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        String newFileName = UUID.randomUUID() + extension;

        File dest = new File(uploadDir, newFileName);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }

        file.transferTo(dest);

        String fileUrl = urlPrefix + newFileName;
        log.info("文件上传成功: {}", fileUrl);

        return fileUrl;
    }
}
