package com.foodie.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodie.dto.user.MerchantQueryDTO;
import com.foodie.entity.Merchant;
import com.foodie.vo.user.MerchantVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MerchantMapper extends BaseMapper<Merchant> {

    /**
     * 分页查询商户（用户端）
     */
    Page<MerchantVO> pageQuery(Page<MerchantVO> page, @Param("query") MerchantQueryDTO query);

    /**
     * 查询商户详情
     */
    MerchantVO getMerchantDetail(@Param("merchantId") Long merchantId);
}