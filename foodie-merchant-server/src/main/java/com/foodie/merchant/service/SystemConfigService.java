package com.foodie.merchant.service;

import java.math.BigDecimal;

/**
 * @Author: wanderer
 * @Date: 2026/1/28 17:18
 */
public interface SystemConfigService {

    BigDecimal getDecimal(String key);
}
