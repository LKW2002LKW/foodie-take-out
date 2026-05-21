package com.foodie.user.controller;

import com.foodie.common.constant.MessageConstant;
import com.foodie.common.result.Result;
import com.foodie.dto.user.AddressDTO;
import com.foodie.user.service.AddressService;
import com.foodie.vo.user.AddressVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 地址管理
 */
@RestController
@RequestMapping("/user/address")
@Api(tags = "地址管理")
@Slf4j
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    /**
     * 新增地址
     */
    @PostMapping
    @ApiOperation("新增地址")
    public Result<String> addAddress(@RequestBody AddressDTO addressDTO,
                                     HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("新增地址：userId={}, addressDTO={}", userId, addressDTO);

        addressService.addAddress(userId, addressDTO);
        return Result.success(MessageConstant.ADDRESS_ADD_SUCCESS);
    }

    /**
     * 修改地址
     */
    @PutMapping
    @ApiOperation("修改地址")
    public Result<String> updateAddress(@RequestBody AddressDTO addressDTO,
                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("修改地址：userId={}, addressDTO={}", userId, addressDTO);

        addressService.updateAddress(userId, addressDTO);
        return Result.success(MessageConstant.ADDRESS_UPDATE_SUCCESS);
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除地址")
    public Result<String> deleteAddress(@PathVariable Long id,
                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("删除地址：userId={}, addressId={}", userId, id);

        addressService.deleteAddress(userId, id);
        return Result.success(MessageConstant.ADDRESS_DELETE_SUCCESS);
    }

    /**
     * 批量删除地址
     */
    @DeleteMapping("/batch")
    @ApiOperation("批量删除地址")
    public Result<String> batchDeleteAddress(@RequestBody List<Long> ids,
                                             HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("批量删除地址：userId={}, ids={}", userId, ids);

        addressService.batchDeleteAddress(userId, ids);
        return Result.success(MessageConstant.ADDRESS_DELETE_SUCCESS);
    }

    /**
     * 根据ID查询地址
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询地址")
    public Result<AddressVO> getAddressById(@PathVariable Long id,
                                            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("查询地址：userId={}, addressId={}", userId, id);

        AddressVO addressVO = addressService.getAddressById(userId, id);
        return Result.success(addressVO);
    }

    /**
     * 查询用户所有地址
     */
    @GetMapping("/list")
    @ApiOperation("查询用户所有地址")
    public Result<List<AddressVO>> listAddress(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("查询地址列表：userId={}", userId);

        List<AddressVO> list = addressService.listAddress(userId);
        return Result.success(list);
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/default/{id}")
    @ApiOperation("设置默认地址")
    public Result<String> setDefaultAddress(@PathVariable Long id,
                                            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("设置默认地址：userId={}, addressId={}", userId, id);

        addressService.setDefaultAddress(userId, id);
        return Result.success(MessageConstant.ADDRESS_SET_DEFAULT_SUCCESS);
    }

    /**
     * 获取默认地址
     */
    @GetMapping("/default")
    @ApiOperation("获取默认地址")
    public Result<AddressVO> getDefaultAddress(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("获取默认地址：userId={}", userId);

        AddressVO addressVO = addressService.getDefaultAddress(userId);
        return Result.success(addressVO);
    }
}
