package com.foodie.vo.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单跟踪VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrackVO implements Serializable {

    private Long orderId;
    private String orderNumber;
    private Integer status;
    private String statusText;
    private LocalDateTime estimatedDeliveryTime;

    /**
     * 跟踪节点列表
     */
    private List<TrackNode> trackNodes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackNode implements Serializable {
        private String title;
        private String description;
        private LocalDateTime time;
        private Boolean completed;
    }
}