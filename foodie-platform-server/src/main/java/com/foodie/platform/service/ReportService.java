package com.foodie.platform.service;


import com.foodie.dto.platform.ReportQueryDTO;
import com.foodie.vo.platform.HotDishVO;
import com.foodie.vo.platform.OrderReportVO;
import com.foodie.vo.platform.RevenueReportVO;
import com.foodie.vo.platform.UserGrowthReportVO;

import java.util.List;

/**
 * 报表服务接口
 */
public interface ReportService {

    /**
     * 营业额报表
     */
    RevenueReportVO getRevenueReport(ReportQueryDTO reportQueryDTO);

    /**
     * 订单统计报表
     */
    OrderReportVO getOrderReport(ReportQueryDTO reportQueryDTO);

    /**
     * 用户增长报表
     */
    UserGrowthReportVO getUserGrowthReport(ReportQueryDTO reportQueryDTO);

    /**
     * 热销商品TOP10
     */
    List<HotDishVO> getHotDishes();
}