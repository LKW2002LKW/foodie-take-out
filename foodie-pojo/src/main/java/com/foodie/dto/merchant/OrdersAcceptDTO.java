package com.foodie.dto.merchant;

import lombok.Data;
import java.io.Serializable;

@Data
public class OrdersAcceptDTO implements Serializable {
    private Long orderId;
    private String action; // ACCEPT / REJECT / COMPLETE
}

