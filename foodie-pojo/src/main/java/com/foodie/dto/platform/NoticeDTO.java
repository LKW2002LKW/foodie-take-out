package com.foodie.dto.platform;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 公告DTO
 */
@Data
public class NoticeDTO implements Serializable {

    @NotBlank(message = "公告标题不能为空")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    private String content;

    @NotNull(message = "公告类型不能为空")
    private Integer type;

    @NotNull(message = "目标对象不能为空")
    private Integer target;
}