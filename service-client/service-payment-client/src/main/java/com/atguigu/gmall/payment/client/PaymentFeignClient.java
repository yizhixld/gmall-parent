package com.atguigu.gmall.payment.client;

import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.client.impl.PaymentFeignClientImpl;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author yizhixld
 * @create 2020-05-10-15:50
 */
@FeignClient(value = "service-payment",fallback = PaymentFeignClientImpl.class)
public interface PaymentFeignClient {
    @GetMapping("api/payment/alipay/closePay/{orderId}")
    public Boolean closePay(@PathVariable Long orderId);


    @RequestMapping("api/payment/alipay/checkPayment/{orderId}")
    public Boolean checkPayment(@PathVariable Long orderId);

    @GetMapping("api/payment/alipay/getPaymentInfo/{outTradeNo}")
    public PaymentInfo getPaymentInfo(@PathVariable String outTradeNo);
}
