package com.foodie.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodie.entity.Category;
import com.foodie.vo.user.CategoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * 查询商户的分类列表（含菜品数量）
     */
    List<CategoryVO> listByMerchant(@Param("merchantId") Long merchantId,
                                    @Param("type") Integer type);
}