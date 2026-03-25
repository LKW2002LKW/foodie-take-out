package com.foodie.dto.merchant;

/**
 * @Author: wanderer
 * @Date: 2026/1/28 17:47
 */
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 员工密码修改DTO
 */
@Data
public class EmployeePasswordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 员工ID
     */
    @NotNull(message = "员工ID不能为空")
    private Long id;

    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = "^.{6,20}$", message = "密码长度必须在6-20位之间")
    private String newPassword;
}