package com.foodie.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.foodie.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单Mapper（平台端）
 */
@Mapper
public interface OrderMapper extends BaseMapper<Orders> {

    /**
     * 统计今日订单数
     */
    @Select("SELECT COUNT(*) FROM orders WHERE DATE(order_time) = CURDATE()")
    Integer countToday();

    /**
     * 统计今日完成订单数
     */
    @Select("SELECT COUNT(*) FROM orders WHERE DATE(order_time) = CURDATE() AND status = 5")
    Integer countTodayCompleted();

    /**
     * 统计今日取消订单数
     */
    @Select("SELECT COUNT(*) FROM orders WHERE DATE(order_time) = CURDATE() AND status = 6")
    Integer countTodayCancelled();

    /**
     * 统计今日交易额
     */
    @Select("SELECT IFNULL(SUM(total_amount), 0) FROM orders WHERE DATE(order_time) = CURDATE() AND status = 5 AND pay_status = 1")
    BigDecimal sumTodayRevenue();

    /**
     * 统计今日平台收入
     */
    @Select("SELECT IFNULL(SUM(platform_commission), 0) FROM orders WHERE DATE(order_time) = CURDATE() AND status = 5 AND pay_status = 1")
    BigDecimal sumTodayPlatformIncome();

    /**
     * 统计待处理订单数（待接单）
     */
    @Select("SELECT COUNT(*) FROM orders WHERE status = 2")
    Integer countPending();

    /**
     * 统计时间段内订单数
     */
    @Select("SELECT COUNT(*) FROM orders WHERE order_time >= #{beginTime} AND order_time <= #{endTime}")
    Integer countByTimePeriod(@Param("beginTime") LocalDateTime beginTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计时间段内交易额
     */
    @Select("SELECT IFNULL(SUM(total_amount), 0) FROM orders WHERE order_time >= #{beginTime} AND order_time <= #{endTime} AND status = 5 AND pay_status = 1")
    BigDecimal sumRevenueByTimePeriod(@Param("beginTime") LocalDateTime beginTime, @Param("endTime") LocalDateTime endTime);
}