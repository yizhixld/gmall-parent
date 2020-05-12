package com.atguigu.gmall.payment.service;

import com.alipay.api.AlipayApiException;

/**
 * @author yizhixld
 * @create 2020-05-08-19:48
 */
public interface AlipayService {
    /**
    * @Description: //生成二维码
    * @Return:
    **/
    String createAliPay(Long orderId) throws AlipayApiException;
    /**
    * @Description: //退款
    * @Return:
    **/
    boolean refund(Long orderId);
    /**
    * @Description: //支付宝关闭交易接口
    * @Return:
    **/
    Boolean closePay(Long orderId);

    /**
    * @Description: //根据订单查询是否支付成功
    * @Return:
    **/
    Boolean checkPayment(Long orderId);
}
