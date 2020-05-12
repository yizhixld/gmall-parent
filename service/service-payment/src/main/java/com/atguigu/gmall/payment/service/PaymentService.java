package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;

import java.util.Map;

/**
 * @author yizhixld
 * @create 2020-05-08-19:32
 */
public interface PaymentService {
    /**
    * @Description: //保存交易状态
    * @Return:
    **/
    void savePaymentInfo(OrderInfo orderInfo,String paymentType);

    /**
    * @Description: //根据订单编号和支付方式确定支付信息
    * @Return:
    **/
    PaymentInfo getPaymentInfo(String outTradeNo, String name);

    /**
    * @Description: //根据订单编号和支付方式以及参数更改交易状态，补充支付信息
    * @Return:
    **/
    void paySuccess(String outTradeNo, String name, Map<String, String> paramsMap);
    /**
    * @Description: //根据交易编号更新交易信息
    * @Return:
    **/
    void updatePaymentInfo(String outTradeNo, PaymentInfo paymentInfo);
    /**
    * @Description: //关闭过期交易记录
    * @Return:
    **/
    void closePayment(Long orderId);
}
