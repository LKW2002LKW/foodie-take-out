package com.foodie.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.foodie.dto.user.AddressDTO;
import com.foodie.entity.AddressBook;
import com.foodie.vo.user.AddressVO;

import java.util.List;

public interface AddressService extends IService<AddressBook> {

    /**
     * 新增地址
     */
    void addAddress(Long userId, AddressDTO addressDTO);

    /**
     * 修改地址
     */
    void updateAddress(Long userId, AddressDTO addressDTO);

    /**
     * 删除地址
     */
    void deleteAddress(Long userId, Long id);

    /**
     * 批量删除地址
     */
    void batchDeleteAddress(Long userId, List<Long> ids);

    /**
     * 根据ID查询地址
     */
    AddressVO getAddressById(Long userId, Long id);

    /**
     * 查询用户所有地址
     */
    List<AddressVO> listAddress(Long userId);

    /**
     * 设置默认地址
     */
    void setDefaultAddress(Long userId, Long id);

    /**
     * 获取默认地址
     */
    AddressVO getDefaultAddress(Long userId);
}