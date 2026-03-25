package com.foodie.merchant.mapper;

/**
 * @Author: wanderer
 * @Date: 2026/1/20 11:57
 */
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商家统计Mapper
 */
@Mapper
public interface MerchantStatisticsMapper {

    /**
     * 统计今日订单总数
     */
    @Select("SELECT COUNT(*) FROM orders " +
            "WHERE merchant_id = #{merchantId} " +
            "AND DATE(order_time) = CURDATE()")
    Integer countTodayOrders(@Param("merchantId") Long merchantId);

    /**
     * 统计今日待接单数
     */
    @Select("SELECT COUNT(*) FROM orders " +
            "WHERE merchant_id = #{merchantId} " +
            "AND status = 2 " +
            "AND DATE(order_time) = CURDATE()")
    Integer countTodayPendingOrders(@Param("merchantId") Long merchantId);

    /**
     * 统计今日已完成数
     */
    @Select("SELECT COUNT(*) FROM orders " +
            "WHERE merchant_id = #{merchantId} " +
            "AND status = 5 " +
            "AND DATE(order_time) = CURDATE()")
    Integer countTodayCompletedOrders(@Param("merchantId") Long merchantId);

    /**
     * 统计今日已取消数
     */
    @Select("SELECT COUNT(*) FROM orders " +
            "WHERE merchant_id = #{merchantId} " +
            "AND status = 6 " +
            "AND DATE(order_time) = CURDATE()")
    Integer countTodayCancelledOrders(@Param("merchantId") Long merchantId);

    /**
     * 统计今日营业额
     */
    @Select("SELECT IFNULL(SUM(total_amount), 0) FROM orders " +
            "WHERE merchant_id = #{merchantId} " +
            "AND status = 5 " +
            "AND pay_status = 1 " +
            "AND DATE(order_time) = CURDATE()")
    BigDecimal sumTodayRevenue(@Param("merchantId") Long merchantId);

    /**
     * 统计今日实收金额
     */
    @Select("SELECT IFNULL(SUM(merchant_amount), 0) FROM orders " +
            "WHERE merchant_id = #{merchantId} " +
            "AND status = 5 " +
            "AND pay_status = 1 " +
            "AND DATE(order_time) = CURDATE()")
    BigDecimal sumTodayIncome(@Param("merchantId") Long merchantId);

    /**
     * 统计今日平台抽成
     */
    @Select("SELECT IFNULL(SUM(platform_commission), 0) FROM orders " +
            "WHERE merchant_id = #{merchantId} " +
            "AND status = 5 " +
            "AND pay_status = 1 " +
            "AND DATE(order_time) = CURDATE()")
    BigDecimal sumTodayCommission(@Param("merchantId") Long merchantId);

    /**
     * 统计指定时间段订单数
     */
    @Select("SELECT COUNT(*) FROM orders " +
            "WHERE merchant_id = #{merchantId} " +
            "AND order_time >= #{beginTime} " +
            "AND order_time <= #{endTime}")
    Integer countOrdersByTime(@Param("merchantId") Long merchantId,
                              @Param("beginTime") LocalDateTime beginTime,
                              @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间段营业额
     */
    @Select("SELECT IFNULL(SUM(total_amount), 0) FROM orders " +
            "WHERE merchant_id = #{merchantId} " +
            "AND status = 5 " +
            "AND pay_status = 1 " +
            "AND order_time >= #{beginTime} " +
            "AND order_time <= #{endTime}")
    BigDecimal sumRevenueByTime(@Param("merchantId") Long merchantId,
                                @Param("beginTime") LocalDateTime beginTime,
                                @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间段实收金额
     */
    @Select("SELECT IFNULL(SUM(merchant_amount), 0) FROM orders " +
            "WHERE merchant_id = #{merchantId} " +
            "AND status = 5 " +
            "AND pay_status = 1 " +
            "AND order_time >= #{beginTime} " +
            "AND order_time <= #{endTime}")
    BigDecimal sumIncomeByTime(@Param("merchantId") Long merchantId,
                               @Param("beginTime") LocalDateTime beginTime,
                               @Param("endTime") LocalDateTime endTime);

    /**
     * 统计累计订单总数
     */
    @Select("SELECT COUNT(*) FROM orders WHERE merchant_id = #{merchantId}")
    Integer countTotalOrders(@Param("merchantId") Long merchantId);

    /**
     * 统计累计完成订单数
     */
    @Select("SELECT COUNT(*) FROM orders " +
            "WHERE merchant_id = #{merchantId} AND status = 5")
    Integer countTotalCompletedOrders(@Param("merchantId") Long merchantId);

    /**
     * 统计累计营业额
     */
    @Select("SELECT IFNULL(SUM(total_amount), 0) FROM orders " +
            "WHERE merchant_id = #{merchantId} " +
            "AND status = 5 AND pay_status = 1")
    BigDecimal sumTotalRevenue(@Param("merchantId") Long merchantId);

    /**
     * 统计累计实收金额
     */
    @Select("SELECT IFNULL(SUM(merchant_amount), 0) FROM orders " +
            "WHERE merchant_id = #{merchantId} " +
            "AND status = 5 AND pay_status = 1")
    BigDecimal sumTotalIncome(@Param("merchantId") Long merchantId);

    /**
     * 统计商户平均评分
     */
    @Select("SELECT IFNULL(AVG(rating), 0) FROM order_review " +
            "WHERE merchant_id = #{merchantId}")
    BigDecimal getAverageRating(@Param("merchantId") Long merchantId);

    /**
     * 统计商户评价总数
     */
    @Select("SELECT COUNT(*) FROM order_review " +
            "WHERE merchant_id = #{merchantId}")
    Integer countTotalReviews(@Param("merchantId") Long merchantId);
}
