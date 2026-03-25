package com.foodie.dto.merchant;

/**
 * @Author: wanderer
 * @Date: 2026/1/22 11:25
 */
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 商户回复DTO
 */
@Data
public class ReviewReplyDTO implements Serializable {

    /**
     * 评价ID
     */
    @NotNull(message = "评价ID不能为空")
    private Long id;

    /**
     * 回复内容
     */
    @NotBlank(message = "回复内容不能为空")
    private String merchantReply;
}