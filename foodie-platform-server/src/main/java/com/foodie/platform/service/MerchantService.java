package com.foodie.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodie.dto.platform.MerchantAuditDTO;
import com.foodie.dto.platform.MerchantPageQueryDTO;
import com.foodie.entity.Merchant;
import com.foodie.vo.platform.MerchantDetailVO;
;

/**
 * 商户服务接口
 */
public interface MerchantService {

    /**
     * 商户分页查询
     * @param merchantPageQueryDTO 查询条件
     * @return 分页结果
     */
    Page<Merchant> pageQuery(MerchantPageQueryDTO merchantPageQueryDTO);

    /**
     * 商户详情
     * @param id 商户ID
     * @return 商户详情
     */
    MerchantDetailVO getDetail(Long id);

    /**
     * 商户审核
     * @param merchantAuditDTO 审核信息
     */
    void audit(MerchantAuditDTO merchantAuditDTO);

    /**
     * 启用商户
     * @param id 商户ID
     */
    void enable(Long id);

    /**
     * 禁用商户
     * @param id 商户ID
     */
    void disable(Long id);
}