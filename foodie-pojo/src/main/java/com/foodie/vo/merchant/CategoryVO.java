package com.foodie.vo.merchant;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class CategoryVO implements Serializable {

    private Long id;
    private Long merchantId;
    private String name;
    private Integer type;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 扩展字段
    private String typeDesc;      // 类型描述
    private String statusDesc;    // 状态描述
}