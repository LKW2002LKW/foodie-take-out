package com.foodie.user.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodie.common.constant.RedisKeyConstant;
import com.foodie.common.constant.StatusConstant;
import com.foodie.dto.user.MerchantQueryDTO;
import com.foodie.entity.Merchant;
import com.foodie.user.mapper.CategoryMapper;
import com.foodie.user.mapper.MerchantMapper;
import com.foodie.user.service.MerchantService;
import com.foodie.vo.user.CategoryVO;
import com.foodie.vo.user.MerchantVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant> implements MerchantService {

    private final CategoryMapper categoryMapper;

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${foodie.cache.merchant-status-ttl-hours:24}")
    private long merchantStatusTtlHours;

    /**
     * 分页查询商户列表
     */
    @Override
    public Page<MerchantVO> pageQuery(MerchantQueryDTO merchantQueryDTO) {
        log.info("查询商户列表：{}", merchantQueryDTO);

        Integer sortType = merchantQueryDTO.getSortType();
        if (sortType == null || sortType < 0 || sortType > 5) {
            merchantQueryDTO.setSortType(0);
        }

        Page<MerchantVO> page = new Page<>(
                merchantQueryDTO.getPage(),
                merchantQueryDTO.getPageSize()
        );

        page = this.baseMapper.pageQuery(page, merchantQueryDTO);

        List<MerchantVO> records = page.getRecords();
        if (records != null && !records.isEmpty()) {
            List<String> keys = new ArrayList<>(records.size());
            Map<String, MerchantVO> keyToVo = new HashMap<>(records.size());
            records.forEach(vo -> {
                String key = String.format(RedisKeyConstant.MERCHANT_STATUS, vo.getId());
                keys.add(key);
                keyToVo.put(key, vo);
            });

            List<Object> cachedStatuses = redisTemplate.opsForValue().multiGet(keys);
            List<String> missingKeys = new ArrayList<>();
            if (cachedStatuses != null) {
                for (int i = 0; i < keys.size(); i++) {
                    Object cached = cachedStatuses.size() > i ? cachedStatuses.get(i) : null;
                    String key = keys.get(i);
                    MerchantVO vo = keyToVo.get(key);
                    if (vo == null) {
                        continue;
                    }
                    if (cached != null) {
                        Integer status = Integer.valueOf(cached.toString());
                        vo.setStatus(status);
                    } else {
                        missingKeys.add(key);
                    }
                }
            } else {
                missingKeys.addAll(keys);
            }

            for (String key : missingKeys) {
                MerchantVO vo = keyToVo.get(key);
                if (vo == null || vo.getStatus() == null) {
                    continue;
                }
                redisTemplate.opsForValue().set(
                        key,
                        vo.getStatus().toString(),
                        merchantStatusTtlHours,
                        TimeUnit.HOURS
                );
            }

            // 设置状态描述
            records.forEach(vo -> {
                vo.setStatusDesc(vo.getStatus().equals(StatusConstant.MERCHANT_OPEN) ? "营业中" : "休息中");
            });
        }

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