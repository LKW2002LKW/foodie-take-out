package com.foodie.merchant.service.impl;

/**
 * @Author: wanderer
 * @Date: 2026/1/28 17:54
 */


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.foodie.common.constant.EmployeeConstant;
import com.foodie.common.exception.BusinessException;
import com.foodie.common.utils.MD5Util;
import com.foodie.dto.merchant.EmployeeAddDTO;
import com.foodie.dto.merchant.EmployeePasswordDTO;
import com.foodie.dto.merchant.EmployeeQueryDTO;
import com.foodie.dto.merchant.EmployeeUpdateDTO;
import com.foodie.entity.MerchantAdmin;
import com.foodie.merchant.mapper.MerchantEmployeeMapper;
import com.foodie.merchant.service.MerchantEmployeeService;

import com.foodie.vo.merchant.MerchantEmployeeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商户员工服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantEmployeeServiceImpl implements MerchantEmployeeService {

    private final MerchantEmployeeMapper merchantEmployeeMapper;

    @Override
    public Map<String, Object> getEmployeePage(EmployeeQueryDTO query) {
        if (query.getMerchantId() == null) {
            throw new BusinessException("商户ID不能为空");
        }

        // 查询员工列表
        List<MerchantEmployeeVO> employees = merchantEmployeeMapper.selectEmployeePage(query);

        // 处理数据
        employees.forEach(employee -> {
            // 设置角色名称
            employee.setRoleName(EmployeeConstant.getRoleName(employee.getRole()));
            // 设置状态名称
            employee.setStatusName(EmployeeConstant.getStatusName(employee.getStatus()));
            // 身份证号脱敏
            if (employee.getIdCard() != null) {
                employee.setIdCard(EmployeeConstant.maskIdCard(employee.getIdCard()));
            }
        });

        // 查询总数
        Long total = merchantEmployeeMapper.countEmployees(query);

        // 计算总页数
        int totalPages = (int) Math.ceil((double) total / query.getPageSize());

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", employees);
        result.put("total", total);
        result.put("page", query.getPage());
        result.put("pageSize", query.getPageSize());
        result.put("totalPages", totalPages);
        result.put("hasNext", query.getPage() < totalPages);

        return result;
    }

    @Override
    public MerchantEmployeeVO getEmployeeDetail(Long employeeId, Long merchantId) {
        if (employeeId == null) {
            throw new BusinessException("员工ID不能为空");
        }
        if (merchantId == null) {
            throw new BusinessException("商户ID不能为空");
        }

        // 查询员工
        MerchantAdmin employee = merchantEmployeeMapper.selectById(employeeId);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }

        // 验证商户权限
        if (!employee.getMerchantId().equals(merchantId)) {
            throw new BusinessException("无权查看该员工信息");
        }

        // 转换为VO
        MerchantEmployeeVO vo = new MerchantEmployeeVO();
        BeanUtils.copyProperties(employee, vo);

        // 设置角色名称和状态名称
        vo.setRoleName(EmployeeConstant.getRoleName(employee.getRole()));
        vo.setStatusName(EmployeeConstant.getStatusName(employee.getStatus()));

        // 身份证号脱敏
        if (vo.getIdCard() != null) {
            vo.setIdCard(EmployeeConstant.maskIdCard(vo.getIdCard()));
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addEmployee(Long merchantId, EmployeeAddDTO dto, Long operatorId) {
        if (merchantId == null) {
            throw new BusinessException("商户ID不能为空");
        }

        // 检查用户名是否已存在
        LambdaQueryWrapper<MerchantAdmin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantAdmin::getMerchantId, merchantId);
        queryWrapper.eq(MerchantAdmin::getUsername, dto.getUsername());
        Long count = merchantEmployeeMapper.selectCount(queryWrapper);

        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 创建员工
        MerchantAdmin employee = new MerchantAdmin();
        BeanUtils.copyProperties(dto, employee);

        employee.setMerchantId(merchantId);
        employee.setPassword(MD5Util.encrypt(dto.getPassword()));
        employee.setStatus(EmployeeConstant.STATUS_NORMAL);
        employee.setCreateUser(operatorId);
        employee.setUpdateUser(operatorId);

        // 保存
        int rows = merchantEmployeeMapper.insert(employee);
        if (rows == 0) {
            throw new BusinessException("新增员工失败");
        }

        log.info("新增员工成功: merchantId={}, username={}", merchantId, dto.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEmployee(Long merchantId, EmployeeUpdateDTO dto, Long operatorId) {
        if (merchantId == null) {
            throw new BusinessException("商户ID不能为空");
        }

        // 查询员工
        MerchantAdmin employee = merchantEmployeeMapper.selectById(dto.getId());
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }

        // 验证商户权限
        if (!employee.getMerchantId().equals(merchantId)) {
            throw new BusinessException("无权修改该员工信息");
        }

        // 更新信息
        employee.setName(dto.getName());
        employee.setPhone(dto.getPhone());
        employee.setIdCard(dto.getIdCard());
        employee.setRole(dto.getRole());
        employee.setStatus(dto.getStatus());
        employee.setUpdateUser(operatorId);

        // 保存
        int rows = merchantEmployeeMapper.updateById(employee);
        if (rows == 0) {
            throw new BusinessException("更新员工失败");
        }

        log.info("更新员工成功: employeeId={}", dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEmployee(Long employeeId, Long merchantId) {
        if (employeeId == null) {
            throw new BusinessException("员工ID不能为空");
        }
        if (merchantId == null) {
            throw new BusinessException("商户ID不能为空");
        }

        // 查询员工
        MerchantAdmin employee = merchantEmployeeMapper.selectById(employeeId);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }

        // 验证商户权限
        if (!employee.getMerchantId().equals(merchantId)) {
            throw new BusinessException("无权删除该员工");
        }

        // 删除
        int rows = merchantEmployeeMapper.deleteById(employeeId);
        if (rows == 0) {
            throw new BusinessException("删除员工失败");
        }

        log.info("删除员工成功: employeeId={}", employeeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long merchantId, EmployeePasswordDTO dto) {
        if (merchantId == null) {
            throw new BusinessException("商户ID不能为空");
        }

        // 查询员工
        MerchantAdmin employee = merchantEmployeeMapper.selectById(dto.getId());
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }

        // 验证商户权限
        if (!employee.getMerchantId().equals(merchantId)) {
            throw new BusinessException("无权重置该员工密码");
        }

        // 重置密码
        String newPasswordMd5 = MD5Util.encrypt(dto.getNewPassword());
        employee.setPassword(newPasswordMd5);

        // 保存
        int rows = merchantEmployeeMapper.updateById(employee);
        if (rows == 0) {
            throw new BusinessException("重置密码失败");
        }

        log.info("重置员工密码成功: employeeId={}", dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long employeeId, Long merchantId, Integer status) {
        if (employeeId == null) {
            throw new BusinessException("员工ID不能为空");
        }
        if (merchantId == null) {
            throw new BusinessException("商户ID不能为空");
        }
        if (status == null) {
            throw new BusinessException("状态不能为空");
        }

        // 查询员工
        MerchantAdmin employee = merchantEmployeeMapper.selectById(employeeId);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }

        // 验证商户权限
        if (!employee.getMerchantId().equals(merchantId)) {
            throw new BusinessException("无权修改该员工状态");
        }

        // 更新状态
        employee.setStatus(status);

        // 保存
        int rows = merchantEmployeeMapper.updateById(employee);
        if (rows == 0) {
            throw new BusinessException("更新状态失败");
        }

        log.info("更新员工状态成功: employeeId={}, status={}", employeeId, status);
    }
}