package com.foodie.vo.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantRegisterVO implements Serializable {

    private Long merchantId;
    private String merchantCode;
    private String message;
}