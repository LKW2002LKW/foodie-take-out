package com.foodie.merchant.service;

/**
 * @Author: wanderer
 * @Date: 2026/1/28 17:51
 */

import com.foodie.dto.merchant.EmployeeAddDTO;
import com.foodie.dto.merchant.EmployeePasswordDTO;
import com.foodie.dto.merchant.EmployeeQueryDTO;
import com.foodie.dto.merchant.EmployeeUpdateDTO;
import com.foodie.vo.merchant.MerchantEmployeeVO;

import java.util.Map;

/**
 * 商户员工服务接口
 */
public interface MerchantEmployeeService {

    /**
     * 分页查询员工列表
     */
    Map<String, Object> getEmployeePage(EmployeeQueryDTO query);

    /**
     * 查询员工详情
     */
    MerchantEmployeeVO getEmployeeDetail(Long employeeId, Long merchantId);

    /**
     * 新增员工
     */
    void addEmployee(Long merchantId, EmployeeAddDTO dto, Long operatorId);

    /**
     * 更新员工信息
     */
    void updateEmployee(Long merchantId, EmployeeUpdateDTO dto, Long operatorId);

    /**
     * 删除员工
     */
    void deleteEmployee(Long employeeId, Long merchantId);

    /**
     * 重置员工密码
     */
    void resetPassword(Long merchantId, EmployeePasswordDTO dto);

    /**
     * 启用/禁用员工
     */
    void updateStatus(Long employeeId, Long merchantId, Integer status);
}