package com.foodie.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.foodie.common.constant.StatusConstant;
import com.foodie.common.exception.BaseException;
import com.foodie.dto.platform.UserPageQueryDTO;
import com.foodie.entity.Orders;
import com.foodie.entity.User;
import com.foodie.platform.mapper.OrderMapper;
import com.foodie.platform.mapper.UserMapper;
import com.foodie.platform.service.UserService;
import com.foodie.vo.platform.UserDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户服务实现（平台端）
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 用户分页查询
     */
    @Override
    public Page<User> pageQuery(UserPageQueryDTO userPageQueryDTO) {
        Page<User> page = new Page<>(userPageQueryDTO.getPage(), userPageQueryDTO.getPageSize());

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(userPageQueryDTO.getPhone()),
                        User::getPhone, userPageQueryDTO.getPhone())
                .like(StringUtils.hasText(userPageQueryDTO.getNickname()),
                        User::getNickname, userPageQueryDTO.getNickname())
                .eq(userPageQueryDTO.getStatus() != null,
                        User::getStatus, userPageQueryDTO.getStatus())
                .orderByDesc(User::getCreateTime);

        return userMapper.selectPage(page, queryWrapper);
    }

    /**
     * 用户详情
     */
    @Override
    public UserDetailVO getDetail(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BaseException("用户不存在");
        }

        UserDetailVO detailVO = new UserDetailVO();
        BeanUtils.copyProperties(user, detailVO);
        detailVO.setSexText(getSexText(user.getSex()));
        detailVO.setStatusText(user.getStatus() == 1 ? "正常" : "禁用");

        // 统计用户订单数据
        LambdaQueryWrapper<Orders> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Orders::getUserId, id);
        Integer totalOrderCount = Math.toIntExact(orderMapper.selectCount(orderWrapper));

        LambdaQueryWrapper<Orders> completedWrapper = new LambdaQueryWrapper<>();
        completedWrapper.eq(Orders::getUserId, id)
                .eq(Orders::getStatus, StatusConstant.ORDER_COMPLETED);
        Integer completedOrderCount = Math.toIntExact(orderMapper.selectCount(completedWrapper));

        // 统计消费金额
        List<Orders> completedOrders = orderMapper.selectList(completedWrapper);
        BigDecimal totalConsumption = completedOrders.stream()
                .map(Orders::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        detailVO.setTotalOrderCount(totalOrderCount);
        detailVO.setCompletedOrderCount(completedOrderCount);
        detailVO.setTotalConsumption(totalConsumption);

        return detailVO;
    }

    /**
     * 启用用户
     */
    @Override
    @Transactional
    public void enableUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BaseException("用户不存在");
        }

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, id)
                .set(User::getStatus, 1);

        userMapper.update(null, updateWrapper);
        log.info("启用用户成功，用户ID：{}", id);
    }

    /**
     * 禁用用户
     */
    @Override
    @Transactional
    public void disableUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BaseException("用户不存在");
        }

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, id)
                .set(User::getStatus, 0);

        userMapper.update(null, updateWrapper);
        log.info("禁用用户成功，用户ID：{}", id);
    }

    /**
     * 获取性别文本
     */
    private String getSexText(Integer sex) {
        if (sex == null) return "未知";
        switch (sex) {
            case 0: return "未知";
            case 1: return "男";
            case 2: return "女";
            default: return "未知";
        }
    }
}