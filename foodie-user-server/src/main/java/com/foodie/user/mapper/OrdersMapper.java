package com.foodie.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodie.entity.Orders;
import com.foodie.vo.user.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

    /**
     * 查询订单详情
     */
    OrderVO getOrderDetail(@Param("orderNumber") String orderNumber, @Param("userId") Long userId);

    /**
     * 订单Mapper接口
     */
    @Mapper
    public interface OrderMapper extends BaseMapper<Orders> {

    }
}