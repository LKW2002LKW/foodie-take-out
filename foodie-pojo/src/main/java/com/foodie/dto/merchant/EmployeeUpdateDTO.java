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
 * 员工更新DTO
 */
@Data
public class EmployeeUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 员工ID
     */
    @NotNull(message = "员工ID不能为空")
    private Long id;

    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空")
    private String name;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 身份证号
     */
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$",
            message = "身份证号格式不正确")
    private String idCard;

    /**
     * 角色 1管理员 2经理 3普通员工
     */
    @NotNull(message = "角色不能为空")
    private Integer role;

    /**
     * 状态 1正常 0禁用
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
