package com.foodie.dto.platform;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 系统配置DTO
 */
@Data
public class ConfigDTO implements Serializable {

    @NotBlank(message = "配置键不能为空")
    private String configKey;

    @NotBlank(message = "配置值不能为空")
    private String configValue;

    private String configDesc;

    private String configType;
}