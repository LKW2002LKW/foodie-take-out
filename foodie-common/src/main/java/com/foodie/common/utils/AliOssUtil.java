package com.foodie.common.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.util.UUID;

@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    /**
     * 文件上传
     *
     * @param bytes 文件字节数组
     * @param objectName 文件路径
     * @return 文件访问URL
     */
    public String upload(byte[] bytes, String objectName) {

        // 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 上传文件
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));

            // 文件访问路径
            String url = "https://" + bucketName + "." + endpoint + "/" + objectName;
            log.info("文件上传成功：{}", url);

            return url;
        } catch (Exception e) {
            log.error("文件上传失败：{}", e.getMessage());
            throw new RuntimeException("文件上传失败");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 生成唯一文件名
     */
    public static String generateFileName(String originalFilename) {
        // 获取文件扩展名
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        // UUID + 扩展名
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }
}