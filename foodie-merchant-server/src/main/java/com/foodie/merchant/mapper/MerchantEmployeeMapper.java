package com.foodie.merchant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.foodie.dto.merchant.EmployeeQueryDTO;
import com.foodie.entity.MerchantAdmin;

import com.foodie.vo.merchant.MerchantEmployeeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商户员工Mapper
 */
@Mapper
public interface MerchantEmployeeMapper extends BaseMapper<MerchantAdmin> {

    /**
     * 分页查询员工列表
     */
    List<MerchantEmployeeVO> selectEmployeePage(@Param("query") EmployeeQueryDTO query);

    /**
     * 查询员工总数
     */
    Long countEmployees(@Param("query") EmployeeQueryDTO query);
}