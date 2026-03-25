package com.foodie.dto.merchant;

import lombok.Data;
import java.io.Serializable;

@Data
public class MerchantBusinessHoursDTO implements Serializable {

    private String businessHours;  // 格式：09:00-22:00
}
