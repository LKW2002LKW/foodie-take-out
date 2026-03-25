package com.foodie.user.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodie.common.constant.StatusConstant;
import com.foodie.dto.user.MerchantQueryDTO;
import com.foodie.entity.Merchant;
import com.foodie.user.mapper.CategoryMapper;
import com.foodie.user.mapper.MerchantMapper;
import com.foodie.user.service.MerchantService;
import com.foodie.vo.user.CategoryVO;
import com.foodie.vo.user.MerchantVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant> implements MerchantService {

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 分页查询商户列表
     */
    @Override
    public Page<MerchantVO> pageQuery(MerchantQueryDTO merchantQueryDTO) {
        log.info("查询商户列表：{}", merchantQueryDTO);

        Page<MerchantVO> page = new Page<>(
                merchantQueryDTO.getPage(),
                merchantQueryDTO.getPageSize()
        );

        page = this.baseMapper.pageQuery(page, merchantQueryDTO);

        // 设置状态描述
        page.getRecords().forEach(vo -> {
            vo.setStatusDesc(vo.getStatus().equals(StatusConstant.MERCHANT_OPEN) ? "营业中" : "休息中");
        });

        return page;
    }

    /**
     * 查询商户详情
     */
    @Override
    public MerchantVO getMerchantDetail(Long merchantId) {
        log.info("查询商户详情：merchantId={}", merchantId);

        MerchantVO merchantVO = this.baseMapper.getMerchantDetail(merchantId);
        if (merchantVO != null) {
            merchantVO.setStatusDesc(merchantVO.getStatus().equals(StatusConstant.MERCHANT_OPEN) ? "营业中" : "休息中");

        }
        return merchantVO;
    }
    /**
     * 查询商户的分类列表
     */
    @Override
    public List<CategoryVO> getMerchantCategories (Long merchantId, Integer type){
        log.info("查询商户分类：merchantId={}, type={}", merchantId, type);

        return categoryMapper.listByMerchant(merchantId, type);
    }
}