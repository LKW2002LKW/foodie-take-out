package com.foodie.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 用户资料更新DTO
 */
@Data
public class UserProfileUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户昵称
     */
    @NotBlank(message = "昵称不能为空")
    private String nickname;

    /**
     * 头像路径
     */
    private String avatar;

    /**
     * 性别 0未知 1男 2女
     */
    private Integer sex;

    /**
     * 生日
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
}