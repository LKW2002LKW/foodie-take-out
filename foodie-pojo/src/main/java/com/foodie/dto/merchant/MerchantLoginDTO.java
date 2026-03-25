package com.foodie.dto.merchant;

import lombok.Data;
import java.io.Serializable;

@Data
public class MerchantLoginDTO implements Serializable {

    private String username;
    private String password;
}