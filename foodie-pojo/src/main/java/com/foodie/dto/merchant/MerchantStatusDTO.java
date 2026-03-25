package com.foodie.dto.merchant;

import lombok.Data;
import java.io.Serializable;

@Data
public class MerchantStatusDTO implements Serializable {

    private Integer status;  // 1营业中 2休息中 3已关闭
}