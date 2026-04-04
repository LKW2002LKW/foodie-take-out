package com.foodie.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodie.common.constant.MessageConstant;
import com.foodie.common.exception.BaseException;
import com.foodie.dto.user.AddressDTO;
import com.foodie.entity.AddressBook;
import com.foodie.user.mapper.AddressMapper;
import com.foodie.user.service.AddressService;
import com.foodie.vo.user.AddressVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AddressServiceImpl extends ServiceImpl<AddressMapper, AddressBook> implements AddressService {

    /**
     * 新增地址
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAddress(Long userId, AddressDTO addressDTO) {
        log.info("新增地址：userId={}, addressDTO={}", userId, addressDTO);

        validateAddressLocation(addressDTO);

        AddressBook addressBook = new AddressBook();

        BeanUtils.copyProperties(addressDTO, addressBook);
        addressBook.setUserId(userId);

        long count = countUserAddress(userId);
        Integer requestIsDefault = addressDTO.getIsDefault();

        if (count == 0) {
            // 首地址强制默认
            addressBook.setIsDefault(1);
        } else if (Integer.valueOf(1).equals(requestIsDefault)) {
            clearUserDefault(userId);
            addressBook.setIsDefault(1);
        } else {
            addressBook.setIsDefault(0);
        }

        this.save(addressBook);

        log.info("地址添加成功：addressId={}", addressBook.getId());
    }

    /**
     * 修改地址
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(Long userId, AddressDTO addressDTO) {
        log.info("修改地址：userId={}, addressDTO={}", userId, addressDTO);

        validateAddressLocation(addressDTO);

        AddressBook addressBook = this.getById(addressDTO.getId());
        if (addressBook == null || !addressBook.getUserId().equals(userId)) {
            throw new BaseException(MessageConstant.ADDRESS_NOT_FOUND);
        }

        Integer originalIsDefault = addressBook.getIsDefault();
        Integer requestIsDefault = addressDTO.getIsDefault();

        BeanUtils.copyProperties(addressDTO, addressBook);
        addressBook.setUserId(userId);

        if (requestIsDefault == null) {
            addressBook.setIsDefault(originalIsDefault);
            this.updateById(addressBook);
            log.info("地址修改成功（默认状态不变）：addressId={}", addressBook.getId());
            return;
        }

        if (Integer.valueOf(1).equals(requestIsDefault)) {
            clearUserDefault(userId);
            addressBook.setIsDefault(1);
            this.updateById(addressBook);
            log.info("地址修改成功（设为默认）：addressId={}", addressBook.getId());
            return;
        }

        if (Integer.valueOf(0).equals(requestIsDefault)) {
            if (Integer.valueOf(1).equals(originalIsDefault) && countUserAddress(userId) == 1) {
                // 仅1条地址时，保持默认
                addressBook.setIsDefault(1);
                this.updateById(addressBook);
                log.info("地址修改成功（仅1条地址保持默认）：addressId={}", addressBook.getId());
                return;
            }

            addressBook.setIsDefault(0);
            this.updateById(addressBook);

            if (Integer.valueOf(1).equals(originalIsDefault)) {
                pickReplacementDefault(userId, addressBook.getId());
            }
            log.info("地址修改成功：addressId={}", addressBook.getId());
            return;
        }

        // 非法值兜底：保持原默认状态
        addressBook.setIsDefault(originalIsDefault);
        this.updateById(addressBook);
        log.info("地址修改成功（非法默认参数已忽略）：addressId={}", addressBook.getId());
    }

    /**
     * 删除地址
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAddress(Long userId, Long id) {
        log.info("删除地址：userId={}, addressId={}", userId, id);

        // 查询地址
        AddressBook addressBook = this.getById(id);
        if (addressBook == null || !addressBook.getUserId().equals(userId)) {
            throw new BaseException(MessageConstant.ADDRESS_NOT_FOUND);
        }

        // 如果删除的是默认地址，需要处理
        boolean isDefault = addressBook.getIsDefault() == 1;

        // 删除地址
        this.removeById(id);

        // 如果删除的是默认地址，补一条默认地址
        if (isDefault) {
            pickReplacementDefault(userId, id);
        }

        log.info("地址删除成功：addressId={}", id);
    }

    /**
     * 批量删除地址
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteAddress(Long userId, List<Long> ids) {
        log.info("批量删除地址：userId={}, ids={}", userId, ids);
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (Long id : ids) {
            if (id == null) {
                continue;
            }
            deleteAddress(userId, id);
        }
    }

    /**
     * 根据ID查询地址
     */
    @Override
    public AddressVO getAddressById(Long userId, Long id) {
        log.info("查询地址：userId={}, addressId={}", userId, id);

        AddressBook addressBook = this.getById(id);
        if (addressBook == null || !addressBook.getUserId().equals(userId)) {
            throw new BaseException(MessageConstant.ADDRESS_NOT_FOUND);
        }

        return convertToVO(addressBook);
    }

    /**
     * 查询用户所有地址
     */
    @Override
    public List<AddressVO> listAddress(Long userId) {
        log.info("查询用户地址列表：userId={}", userId);

        LambdaQueryWrapper<AddressBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AddressBook::getUserId, userId)
                .orderByDesc(AddressBook::getIsDefault)  // 默认地址排在前面
                .orderByDesc(AddressBook::getCreateTime);

        List<AddressBook> addressBooks = this.list(wrapper);

        return addressBooks.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 设置默认地址
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultAddress(Long userId, Long id) {
        log.info("设置默认地址：userId={}, addressId={}", userId, id);

        // 查询地址
        AddressBook addressBook = this.getById(id);
        if (addressBook == null || !addressBook.getUserId().equals(userId)) {
            throw new BaseException(MessageConstant.ADDRESS_NOT_FOUND);
        }

        clearUserDefault(userId);
        addressBook.setIsDefault(1);
        this.updateById(addressBook);

        log.info("默认地址设置成功：addressId={}", id);
    }

    /**
     * 获取默认地址
     */
    @Override
    public AddressVO getDefaultAddress(Long userId) {
        log.info("获取默认地址：userId={}", userId);

        LambdaQueryWrapper<AddressBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AddressBook::getUserId, userId)
                .eq(AddressBook::getIsDefault, 1);

        AddressBook addressBook = this.getOne(wrapper);

        return addressBook != null ? convertToVO(addressBook) : null;
    }

    private long countUserAddress(Long userId) {
        LambdaQueryWrapper<AddressBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AddressBook::getUserId, userId);
        return this.count(wrapper);
    }

    private void clearUserDefault(Long userId) {
        LambdaUpdateWrapper<AddressBook> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AddressBook::getUserId, userId)
                .set(AddressBook::getIsDefault, 0);
        this.update(updateWrapper);
    }

    private void pickReplacementDefault(Long userId, Long excludeId) {
        LambdaQueryWrapper<AddressBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AddressBook::getUserId, userId)
                .ne(excludeId != null, AddressBook::getId, excludeId)
                .orderByAsc(AddressBook::getCreateTime)
                .orderByAsc(AddressBook::getId)
                .last("LIMIT 1");

        AddressBook replacement = this.getOne(wrapper);
        if (replacement != null) {
            clearUserDefault(userId);
            replacement.setIsDefault(1);
            this.updateById(replacement);
        }
    }

    private void validateAddressLocation(AddressDTO addressDTO) {
        if (addressDTO.getLongitude() == null || addressDTO.getLatitude() == null) {
            throw new BaseException(MessageConstant.ADDRESS_LOCATION_REQUIRED);
        }
        if (isBlank(addressDTO.getProvinceName())
                || isBlank(addressDTO.getCityName())
                || isBlank(addressDTO.getDistrictName())
                || isBlank(addressDTO.getDetail())) {
            throw new BaseException(MessageConstant.ADDRESS_LOCATION_REQUIRED);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * 转换为VO
     */
    public AddressVO convertToVO(AddressBook addressBook) {
        AddressVO vo = new AddressVO();
        BeanUtils.copyProperties(addressBook, vo);

        // 拼接完整地址
        String fullAddress = (addressBook.getProvinceName() != null ? addressBook.getProvinceName() : "") +
                (addressBook.getCityName() != null ? addressBook.getCityName() : "") +
                (addressBook.getDistrictName() != null ? addressBook.getDistrictName() : "") +
                (addressBook.getDetail() != null ? addressBook.getDetail() : "");
        vo.setFullAddress(fullAddress);

        return vo;
    }
}