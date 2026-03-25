package com.foodie.vo.platform;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 用户增长报表VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGrowthReportVO implements Serializable {

    /**
     * 日期列表
     */
    private List<String> dateList;

    /**
     * 新增用户数列表
     */
    private List<Integer> newUserCountList;

    /**
     * 总用户数列表
     */
    private List<Integer> totalUserCountList;

    /**
     * 总新增用户数
     */
    private Integer totalNewUserCount;
}