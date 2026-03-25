package com.foodie.merchant.controller;

import com.foodie.common.constant.MessageConstant;
import com.foodie.common.context.BaseContext;
import com.foodie.common.properties.AliOssProperties;
import com.foodie.common.result.Result;
import com.foodie.common.utils.AliOssUtil;
import com.foodie.dto.merchant.MerchantBusinessHoursDTO;
import com.foodie.dto.merchant.MerchantStatusDTO;
import com.foodie.dto.merchant.MerchantUpdateDTO;
import com.foodie.merchant.service.MerchantService;
import com.foodie.vo.merchant.MerchantVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 商户信息管理
 */
@RestController
@RequestMapping("/merchant/info")
@Api(tags = "商户信息管理")
@Slf4j
public class MerchantInfoController {

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private AliOssProperties aliOssProperties;

    @Autowired
    private CommonController commonController;


    /**
     * 获取当前商户信息
     */
    @GetMapping("/current")
    @ApiOperation("获取当前商户信息")
    public Result<MerchantVO> getCurrentMerchant(HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("merchantId");
        log.info("获取商户信息，merchantId={}", merchantId);

        MerchantVO merchantVO = merchantService.getMerchantInfo(merchantId);
        return Result.success(merchantVO);
    }

    /**
     * 修改商户信息
     */
    @PutMapping
    @ApiOperation("修改商户信息")
    public Result<String> updateMerchant(@RequestBody MerchantUpdateDTO merchantUpdateDTO,
                                         HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("merchantId");
        log.info("修改商户信息，merchantId={}, merchantUpdateDTO={}", merchantId, merchantUpdateDTO);

        merchantService.updateMerchantInfo(merchantId, merchantUpdateDTO);
        return Result.success(MessageConstant.MERCHANT_INFO_UPDATE_SUCCESS);
    }

    /**
     * 修改营业状态
     */
    @PutMapping("/status")
    @ApiOperation("修改营业状态")
    public Result<String> updateStatus(@RequestBody MerchantStatusDTO merchantStatusDTO,
                                       HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("merchantId");
        log.info("修改营业状态，merchantId={}, status={}", merchantId, merchantStatusDTO.getStatus());

        merchantService.updateStatus(merchantId, merchantStatusDTO);
        return Result.success(MessageConstant.MERCHANT_STATUS_UPDATE_SUCCESS);
    }

    /**
     * 修改营业时间
     */
    @PutMapping("/businessHours")
    @ApiOperation("修改营业时间")
    public Result<String> updateBusinessHours(@RequestBody MerchantBusinessHoursDTO merchantBusinessHoursDTO,
                                              HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("merchantId");
        log.info("修改营业时间，merchantId={}, businessHours={}", merchantId, merchantBusinessHoursDTO.getBusinessHours());

        merchantService.updateBusinessHours(merchantId, merchantBusinessHoursDTO);
        return Result.success(MessageConstant.MERCHANT_BUSINESS_HOURS_UPDATE_SUCCESS);
    }

    /**
     * 上传商户Logo
     */


    @PostMapping("/upload/logo")
    @ApiOperation("上传商户Logo")
    public Result uploadLogo(@RequestParam("file") MultipartFile file) throws IOException {
        log.info("上传商户Logo，文件名：{}", file.getOriginalFilename());

        /*try {
            // 检查文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null ||
                    (!originalFilename.endsWith(".jpg") &&
                            !originalFilename.endsWith(".jpeg") &&
                            !originalFilename.endsWith(".png"))) {
                return Result.error(MessageConstant.FILE_TYPE_ERROR);
            }

            // 生成文件名
            String fileName = AliOssUtil.generateFileName(originalFilename);
            String objectName = aliOssProperties.getUploadPath() + fileName;

            // 上传文件
            AliOssUtil aliOssUtil = new AliOssUtil(
                    aliOssProperties.getEndpoint(),
                    aliOssProperties.getAccessKeyId(),
                    aliOssProperties.getAccessKeySecret(),
                    aliOssProperties.getBucketName()
            );

            String url = aliOssUtil.upload(file.getBytes(), objectName);

            return Result.success(url);

        } catch (IOException e) {
            log.error("文件上传失败：{}", e.getMessage());
            return Result.error(MessageConstant.FILE_UPLOAD_FAILED);
        }
    }*/

        // 1️⃣ 上传图片，获取 URL
        Result uploadResult = commonController.upload(file);

        String logoUrl = (String) uploadResult.getData();

        // 2️⃣ 更新数据库
        Long merchantId = BaseContext.getCurrentId(); // 获取当前商户ID
        merchantService.updateLogo(merchantId, logoUrl);

        // 3️⃣ 返回最终结果
        return Result.success(logoUrl);
    }
}