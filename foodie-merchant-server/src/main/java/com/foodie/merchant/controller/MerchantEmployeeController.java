package com.foodie.merchant.controller;

/**
 * @Author: wanderer
 * @Date: 2026/1/29 11:41
 */


import com.foodie.common.context.BaseContext;
import com.foodie.common.result.Result;
import com.foodie.dto.merchant.EmployeeAddDTO;
import com.foodie.dto.merchant.EmployeePasswordDTO;
import com.foodie.dto.merchant.EmployeeQueryDTO;
import com.foodie.dto.merchant.EmployeeUpdateDTO;
import com.foodie.merchant.service.MerchantEmployeeService;

import com.foodie.vo.merchant.MerchantEmployeeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 商户员工管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/merchant/employee")
@Api(tags = "商户端-员工管理")
public class MerchantEmployeeController {

    @Resource
    private MerchantEmployeeService merchantEmployeeService;

    /**
     * 分页查询员工列表
     */
    @GetMapping("/page")
    @ApiOperation("分页查询员工列表")
    public Result<Map<String, Object>> getEmployeePage(
            @ApiParam("关键字") @RequestParam(required = false) String keyword,
            @ApiParam("角色") @RequestParam(required = false) Integer role,
            @ApiParam("状态") @RequestParam(required = false) Integer status,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize)
            {
                Long merchantId = BaseContext.getCurrentId();
        log.info("查询员工列表: merchantId={}, keyword={}, role={}, status={}, page={}, pageSize={}",
                merchantId, keyword, role, status, page, pageSize);

        // 构建查询参数
        EmployeeQueryDTO query = new EmployeeQueryDTO();
        query.setMerchantId(merchantId);
        query.setKeyword(keyword);
        query.setRole(role);
        query.setStatus(status);
        query.setPage(page);
        query.setPageSize(pageSize);

        // 查询数据
        Map<String, Object> result = merchantEmployeeService.getEmployeePage(query);

        return Result.success(result);
    }

    /**
     * 查询员工详情
     */
    @GetMapping("/{employeeId}")
    @ApiOperation("查询员工详情")
    public Result<MerchantEmployeeVO> getEmployeeDetail(
            @ApiParam("员工ID") @PathVariable Long employeeId
           ) {
        Long merchantId = BaseContext.getCurrentId();
        log.info("查询员工详情: employeeId={}, merchantId={}", employeeId, merchantId);

        MerchantEmployeeVO employee = merchantEmployeeService.getEmployeeDetail(employeeId, merchantId);

        return Result.success(employee);
    }

    /**
     * 新增员工
     */
    @PostMapping("/add")
    @ApiOperation("新增员工")
    public Result<String> addEmployee(
            @Validated @RequestBody EmployeeAddDTO dto,
            @RequestHeader("merchantAdminId") Long operatorId) {
        Long merchantId = BaseContext.getCurrentId();
        log.info("新增员工: merchantId={}, dto={}", merchantId, dto);

        merchantEmployeeService.addEmployee(merchantId, dto, operatorId);

        return Result.success("新增成功");
    }

    /**
     * 更新员工信息
     */
    @PutMapping("/update")
    @ApiOperation("更新员工信息")
    public Result<String> updateEmployee(
            @Validated @RequestBody EmployeeUpdateDTO dto,
            @RequestHeader("merchantAdminId") Long operatorId) {
        Long merchantId = BaseContext.getCurrentId();
        log.info("更新员工信息: merchantId={}, dto={}", merchantId, dto);

        merchantEmployeeService.updateEmployee(merchantId, dto, operatorId);

        return Result.success("更新成功");
    }

    /**
     * 删除员工
     */
    @DeleteMapping("/{employeeId}")
    @ApiOperation("删除员工")
    public Result<String> deleteEmployee(
            @ApiParam("员工ID") @PathVariable Long employeeId) {
        Long merchantId = BaseContext.getCurrentId();
        log.info("删除员工: employeeId={}, merchantId={}", employeeId, merchantId);

        merchantEmployeeService.deleteEmployee(employeeId, merchantId);

        return Result.success("删除成功");
    }

    /**
     * 重置员工密码
     */
    @PutMapping("/reset-password")
    @ApiOperation("重置员工密码")
    public Result<String> resetPassword(
            @Validated @RequestBody EmployeePasswordDTO dto) {
        Long merchantId = BaseContext.getCurrentId();
        log.info("重置员工密码: merchantId={}, employeeId={}", merchantId, dto.getId());

        merchantEmployeeService.resetPassword(merchantId, dto);

        return Result.success("密码重置成功");
    }

    /**
     * 启用/禁用员工
     */
    @PutMapping("/status/{employeeId}")
    @ApiOperation("启用/禁用员工")
    public Result<String> updateStatus(
            @ApiParam("员工ID") @PathVariable Long employeeId,
            @ApiParam("状态(1-正常 0-禁用)") @RequestParam Integer status) {
        Long merchantId = BaseContext.getCurrentId();
        log.info("更新员工状态: employeeId={}, status={}, merchantId={}", employeeId, status, merchantId);

        merchantEmployeeService.updateStatus(employeeId, merchantId, status);

        return Result.success("操作成功");
    }
}