package com.foodie.common.service.impl;

import com.foodie.common.properties.MinioProperties;
import com.foodie.common.service.FileStorageService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
public class MinioFileStorageService implements FileStorageService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final MinioClient minioClient;
    private final MinioProperties properties;

    public MinioFileStorageService(MinioClient minioClient,
                                   MinioProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    @Override
    public String upload(MultipartFile file, String service) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        validateFile(extension, file.getSize());

        String objectKey = buildObjectKey(service, extension);
        ensureBucket();

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(objectKey)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            log.error("MinIO 上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败", e);
        }

        String publicUrl = StringUtils.removeEnd(properties.getPublicUrl(), "/");
        return publicUrl + "/" + objectKey;
    }

    private void validateFile(String extension, long size) {
        if (extension == null) {
            throw new RuntimeException("文件类型不合法");
        }
        String extLower = extension.toLowerCase(Locale.ROOT);
        if (!properties.getAllowedTypes().contains(extLower)) {
            throw new RuntimeException("文件类型不合法");
        }
        long maxBytes = properties.getMaxSize() * 1024 * 1024;
        if (size > maxBytes) {
            throw new RuntimeException("文件过大");
        }
    }

    private String buildObjectKey(String service, String extension) {
        String basePath = StringUtils.defaultIfBlank(properties.getBasePath(), "upload");
        String servicePath = StringUtils.defaultIfBlank(service, "common");
        String datePath = LocalDate.now().format(DATE_FORMATTER);
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        return String.join("/", basePath, servicePath, datePath, filename);
    }

    private String getExtension(String originalFilename) {
        if (StringUtils.isBlank(originalFilename) || !originalFilename.contains(".")) {
            return null;
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
    }

    private void ensureBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(properties.getBucket())
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(properties.getBucket())
                        .build());
            }
        } catch (Exception e) {
            log.error("MinIO bucket 检查/创建失败: {}", e.getMessage(), e);
            throw new RuntimeException("存储初始化失败", e);
        }
    }
}
