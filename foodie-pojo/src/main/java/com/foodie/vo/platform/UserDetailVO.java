package com.foodie.vo.platform;

/**
 * @Author: wanderer
 * @Date: 2026/1/16 16:47
 */
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户详情VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailVO implements Serializable {

    private Long id;
    private String phone;
    private String nickname;
    private String avatar;
    private Integer sex;
    private String sexText;
    private LocalDate birthday;
    private Integer status;
    private String statusText;
    private LocalDateTime createTime;

    /**
     * 用户统计数据
     */
    private Integer totalOrderCount;
    private Integer completedOrderCount;
    private java.math.BigDecimal totalConsumption;
}