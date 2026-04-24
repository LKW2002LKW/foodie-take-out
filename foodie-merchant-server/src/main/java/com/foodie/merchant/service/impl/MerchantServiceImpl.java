package com.foodie.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodie.common.constant.MessageConstant;
import com.foodie.common.constant.RedisKeyConstant;
import com.foodie.common.constant.StatusConstant;
import com.foodie.common.exception.BaseException;
import com.foodie.dto.merchant.MerchantBusinessHoursDTO;
import com.foodie.dto.merchant.MerchantStatusDTO;
import com.foodie.dto.merchant.MerchantUpdateDTO;
import com.foodie.entity.Merchant;
import com.foodie.merchant.mapper.MerchantMapper;
import com.foodie.merchant.service.MerchantService;
import com.foodie.vo.merchant.MerchantVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant> implements MerchantService {

    @Value("${foodie.cache.merchant-status-ttl-hours:24}")
    private long merchantStatusTtlHours;

    private final RedisTemplate<String, Object> redisTemplate;

    public MerchantServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取商户信息
     */
    @Override
    public MerchantVO getMerchantInfo(Long merchantId) {
        log.info("获取商户信息：merchantId={}", merchantId);

        Merchant merchant = this.getById(merchantId);
        if (merchant == null) {
            throw new BaseException(MessageConstant.MERCHANT_NOT_FOUND);
        }

        MerchantVO merchantVO = new MerchantVO();
        BeanUtils.copyProperties(merchant, merchantVO);

        // 设置状态描述
        merchantVO.setStatusDesc(getStatusDesc(merchant.getStatus()));
        merchantVO.setAuditStatusDesc(getAuditStatusDesc(merchant.getAuditStatus()));

        return merchantVO;
    }

    /**
     * 修改商户信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMerchantInfo(Long merchantId, MerchantUpdateDTO merchantUpdateDTO) {
        log.info("修改商户信息：merchantId={}, merchantUpdateDTO={}", merchantId, merchantUpdateDTO);

        // 查询商户
        Merchant merchant = this.getById(merchantId);
        if (merchant == null) {
            throw new BaseException(MessageConstant.MERCHANT_NOT_FOUND);
        }

        // 检查审核状态（审核中的商户不允许修改）
        if (merchant.getAuditStatus().equals(StatusConstant.AUDIT_PENDING)) {
            throw new BaseException(MessageConstant.MERCHANT_AUDIT_PENDING);
        }

        LambdaUpdateWrapper<Merchant> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Merchant::getId, merchantId)
                .set(merchantUpdateDTO.getMerchantName() != null, Merchant::getMerchantName, merchantUpdateDTO.getMerchantName())
                .set(merchantUpdateDTO.getContactName() != null, Merchant::getContactName, merchantUpdateDTO.getContactName())
                .set(merchantUpdateDTO.getContactPhone() != null, Merchant::getContactPhone, merchantUpdateDTO.getContactPhone())
                .set(merchantUpdateDTO.getProvinceCode() != null, Merchant::getProvinceCode, merchantUpdateDTO.getProvinceCode())
                .set(merchantUpdateDTO.getProvinceName() != null, Merchant::getProvinceName, merchantUpdateDTO.getProvinceName())
                .set(merchantUpdateDTO.getCityCode() != null, Merchant::getCityCode, merchantUpdateDTO.getCityCode())
                .set(merchantUpdateDTO.getCityName() != null, Merchant::getCityName, merchantUpdateDTO.getCityName())
                .set(merchantUpdateDTO.getDistrictCode() != null, Merchant::getDistrictCode, merchantUpdateDTO.getDistrictCode())
                .set(merchantUpdateDTO.getDistrictName() != null, Merchant::getDistrictName, merchantUpdateDTO.getDistrictName())
                .set(merchantUpdateDTO.getAddress() != null, Merchant::getAddress, merchantUpdateDTO.getAddress())
                .set(merchantUpdateDTO.getLongitude() != null, Merchant::getLongitude, merchantUpdateDTO.getLongitude())
                .set(merchantUpdateDTO.getLatitude() != null, Merchant::getLatitude, merchantUpdateDTO.getLatitude())
                .set(merchantUpdateDTO.getLogo() != null, Merchant::getLogo, merchantUpdateDTO.getLogo())
                .set(merchantUpdateDTO.getDescription() != null, Merchant::getDescription, merchantUpdateDTO.getDescription())
                .set(merchantUpdateDTO.getBizCategoryId() != null, Merchant::getBizCategoryId, merchantUpdateDTO.getBizCategoryId())
                .set(merchantUpdateDTO.getBusinessHours() != null, Merchant::getBusinessHours, merchantUpdateDTO.getBusinessHours())
                .set(merchantUpdateDTO.getMinDeliveryAmount() != null, Merchant::getMinDeliveryAmount, merchantUpdateDTO.getMinDeliveryAmount())
                .set(merchantUpdateDTO.getDeliveryFee() != null, Merchant::getDeliveryFee, merchantUpdateDTO.getDeliveryFee());

        this.update(updateWrapper);

        log.info("商户信息修改成功：merchantId={}", merchantId);
    }

    /**
     * 修改营业状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long merchantId, MerchantStatusDTO merchantStatusDTO) {
        log.info("修改营业状态：merchantId={}, status={}", merchantId, merchantStatusDTO.getStatus());

        Merchant merchant = this.getById(merchantId);
        if (merchant == null) {
            throw new BaseException(MessageConstant.MERCHANT_NOT_FOUND);
        }

        // 检查审核状态
        if (!merchant.getAuditStatus().equals(StatusConstant.AUDIT_PASSED)) {
            throw new BaseException(MessageConstant.MERCHANT_NOT_APPROVED);
        }

        // 更新状态
        merchant.setStatus(merchantStatusDTO.getStatus());
        this.updateById(merchant);

        String cacheKey = String.format(RedisKeyConstant.MERCHANT_STATUS, merchantId);
        try {
            redisTemplate.opsForValue().set(
                    cacheKey,
                    merchantStatusDTO.getStatus().toString(),
                    merchantStatusTtlHours,
                    TimeUnit.HOURS
            );
        } catch (Exception e) {
            log.warn("更新商户状态缓存失败：merchantId={}, status={}", merchantId, merchantStatusDTO.getStatus(), e);
        }

        log.info("营业状态修改成功：merchantId={}, status={}", merchantId, merchantStatusDTO.getStatus());
    }

    /**
     * 修改营业时间
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBusinessHours(Long merchantId, MerchantBusinessHoursDTO merchantBusinessHoursDTO) {
        log.info("修改营业时间：merchantId={}, businessHours={}", merchantId, merchantBusinessHoursDTO.getBusinessHours());

        Merchant merchant = this.getById(merchantId);
        if (merchant == null) {
            throw new BaseException(MessageConstant.MERCHANT_NOT_FOUND);
        }

        // 更新营业时间
        merchant.setBusinessHours(merchantBusinessHoursDTO.getBusinessHours());
        this.updateById(merchant);

        log.info("营业时间修改成功：merchantId={}, businessHours={}", merchantId, merchantBusinessHoursDTO.getBusinessHours());
    }

    @Override
    @Transactional
    public void updateLogo(Long merchantId, String logoUrl) {
        // 方法2：也可以用 UpdateWrapper（可选）
        UpdateWrapper<Merchant> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", merchantId);
        wrapper.set("logo", logoUrl);
        this.update(wrapper);
    }

    /**
     * 获取状态描述
     */
    private String getStatusDesc(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0: return "待审核";
            case 1: return "营业中";
            case 2: return "休息中";
            case 3: return "已关闭";
            default: return "未知";
        }
    }

    /**
     * 获取审核状态描述
     */
    private String getAuditStatusDesc(Integer auditStatus) {
        if (auditStatus == null) {
            return "未知";
        }
        switch (auditStatus) {
            case 1: return "已通过";
            case 2: return "审核中";
            case 3: return "已拒绝";
            default: return "未知";
        }
    }
}
