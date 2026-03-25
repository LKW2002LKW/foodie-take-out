package com.foodie.platform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 报表专用Mapper
 */
@Mapper
public interface ReportMapper {

    /**
     * 统计指定时间段内的营业额
     */
    @Select("SELECT IFNULL(SUM(total_amount), 0) FROM orders " +
            "WHERE status = 5 AND pay_status = 1 " +
            "AND order_time >= #{beginTime} AND order_time <= #{endTime}")
    BigDecimal sumRevenue(@Param("beginTime") LocalDateTime beginTime,
                          @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间段内的订单数
     */
    @Select("SELECT COUNT(*) FROM orders " +
            "WHERE order_time >= #{beginTime} AND order_time <= #{endTime}")
    Integer countOrders(@Param("beginTime") LocalDateTime beginTime,
                        @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间段内的有效订单数
     */
    @Select("SELECT COUNT(*) FROM orders " +
            "WHERE status = 5 AND pay_status = 1 " +
            "AND order_time >= #{beginTime} AND order_time <= #{endTime}")
    Integer countValidOrders(@Param("beginTime") LocalDateTime beginTime,
                             @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间段内的新增用户数
     */
    @Select("SELECT COUNT(*) FROM user " +
            "WHERE create_time >= #{beginTime} AND create_time <= #{endTime}")
    Integer countNewUsers(@Param("beginTime") LocalDateTime beginTime,
                          @Param("endTime") LocalDateTime endTime);

    /**
     * 查询热销商品TOP10
     */
    @Select("SELECT od.dish_id, od.name AS dish_name, d.image AS dish_image, " +
            "d.merchant_id, m.merchant_name, SUM(od.number) AS sales_count " +
            "FROM order_detail od " +
            "JOIN orders o ON od.order_id = o.id " +
            "LEFT JOIN dish d ON od.dish_id = d.id " +
            "LEFT JOIN merchant m ON d.merchant_id = m.id " +
            "WHERE o.status = 5 AND o.pay_status = 1 AND od.dish_id IS NOT NULL " +
            "GROUP BY od.dish_id, od.name, d.image, d.merchant_id, m.merchant_name " +
            "ORDER BY sales_count DESC " +
            "LIMIT 10")
    List<Map<String, Object>> getHotDishes();
}