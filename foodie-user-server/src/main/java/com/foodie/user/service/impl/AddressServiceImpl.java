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

        AddressBook addressBook = new AddressBook();
        BeanUtils.copyProperties(addressDTO, addressBook);
        addressBook.setUserId(userId);

        // 如果是第一个地址，自动设为默认
        LambdaQueryWrapper<AddressBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AddressBook::getUserId, userId);
        long count = this.count(wrapper);

        addressBook.setIsDefault(count == 0 ? 1 : 0);

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

        // 查询地址
        AddressBook addressBook = this.getById(addressDTO.getId());
        if (addressBook == null || !addressBook.getUserId().equals(userId)) {
            throw new BaseException(MessageConstant.ADDRESS_NOT_FOUND);
        }

        // 更新地址
        BeanUtils.copyProperties(addressDTO, addressBook);
        this.updateById(addressBook);

        log.info("地址修改成功：addressId={}", addressBook.getId());
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

        // 如果删除的是默认地址，将第一个地址设为默认
        if (isDefault) {
            LambdaQueryWrapper<AddressBook> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AddressBook::getUserId, userId)
                    .orderByAsc(AddressBook::getCreateTime)
                    .last("LIMIT 1");
            AddressBook firstAddress = this.getOne(wrapper);

            if (firstAddress != null) {
                firstAddress.setIsDefault(1);
                this.updateById(firstAddress);
            }
        }

        log.info("地址删除成功：addressId={}", id);
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

        // 将该用户的所有地址设为非默认
        LambdaUpdateWrapper<AddressBook> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AddressBook::getUserId, userId)
                .set(AddressBook::getIsDefault, 0);
        this.update(updateWrapper);

        // 将指定地址设为默认
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