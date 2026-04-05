package com.foodie.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodie.dto.user.OrdersPageQueryDTO;
import com.foodie.entity.Orders;
import com.foodie.vo.user.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

    /**
     * 用户订单分页查询
     */
    Page<OrderVO> pageQuery(Page<OrderVO> page,
                            @Param("query") OrdersPageQueryDTO query,
                            @Param("userId") Long userId);

    /**
     * 查询订单详情
     */
    OrderVO getOrderDetail(@Param("orderNumber") String orderNumber, @Param("userId") Long userId);

}