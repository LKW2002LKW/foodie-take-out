package com.foodie.vo.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaymentVO implements Serializable {

    private String orderNumber;
    private String payUrl;          // 支付二维码URL（PC端）
    private String nonceStr;        // 随机字符串
    private String paySign;         // 支付签名
    private String signType;        // 签名类型
    private String timeStamp;       // 时间戳
    private String packageStr;      // 统一下单接口返回的prepay_id参数值
}
