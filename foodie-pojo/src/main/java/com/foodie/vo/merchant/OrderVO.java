package com.foodie.vo.merchant;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class OrderVO implements Serializable {
    private Long id;
    private String orderNumber;
    private LocalDateTime createTime;
}


