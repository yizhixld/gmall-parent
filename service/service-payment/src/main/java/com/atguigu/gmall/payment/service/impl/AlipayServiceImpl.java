package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.atguigu.gmall.common.util.FiltratHttpsUtils;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.service.AlipayService;
import com.atguigu.gmall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @author yizhixld
 * @create 2020-05-08-19:49
 */
@Service
public class AlipayServiceImpl implements AlipayService {
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderFeignClient orderFeignClient;

    @Autowired
    private AlipayClient alipayClient;


    @Override
    public String createAliPay(Long orderId) throws AlipayApiException {
        // 获取订单信息
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);
        // 保存交易记录
        paymentService.savePaymentInfo(orderInfo, PaymentType.ALIPAY.name());
        // 生成二维码
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest(); //创建API对应的request
        // 同步回调
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        // 异步回调
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url); //在公共参数中设置回跳和通知地址
        // 声明map，存放参数
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("out_trade_no", orderInfo.getOutTradeNo());
        hashMap.put("product_code", "FAST_INSTANT_TRADE_PAY");
        hashMap.put("total_amount", orderInfo.getTotalAmount());
        hashMap.put("subject", orderInfo.getTradeBody());
//        alipayRequest.setBizContent( "{"  +
//                "    \"out_trade_no\":\"20150320010101001\","  +
//                "    \"product_code\":\"FAST_INSTANT_TRADE_PAY\","  +
//                "    \"total_amount\":88.88,"  +
//                "    \"subject\":\"Iphone6 16G\","  +
//                "    \"body\":\"Iphone6 16G\","  +
//                "    \"passback_params\":\"merchantBizType%3d3C%26merchantBizNo%3d2016010101111\","  +
//                "    \"extend_params\":{"  +
//                "    \"sys_service_provider_id\":\"2088511833207846\""  +
//                "    }" +
//                "  }" ); //填充业务参数

        alipayRequest.setBizContent(JSON.toJSONString(hashMap));
        return alipayClient.pageExecute(alipayRequest).getBody();  //调用SDK生成表单
    }

    @Override
    public boolean refund(Long orderId) {
//        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
//        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);
//        HashMap<String, Object> hashMap = new HashMap<>();
//        hashMap.put("out_trade_no", orderInfo.getOutTradeNo());
//        hashMap.put("refund_amount", orderInfo.getTotalAmount());
//        hashMap.put("refund_reason", "下单错误");
//
////        request.setBizContent("{" +
////                "\"trade_no\":\"2014112611001004680073956707\"," +
////                "\"out_trade_no\":\"20150320010101001\"," +
////                "\"out_request_no\":\"HZ01RF001\"," +
////                "\"refund_amount\":200.12," +
////                "\"biz_type\":\"CREDIT_REFUND\"," +
////                "\"refund_reason\":\"正常退款\"," +
////                "\"operator_id\":\"OP001\"," +
////                "\"store_id\":\"NJ_S_001\"," +
////                "\"terminal_id\":\"NJ_T_001\"," +
////                "\"extend_params\":{" +
////                "\"credit_service_id\":\"2019031400000000000000369900\"," +
////                "\"credit_category_id\":\"REFUND\"" +
////                "    }" +
////                "  }");
//
//        request.setBizContent(JSON.toJSONString(hashMap));
//        AlipayTradeRefundResponse response = null;
//        try {
//            response = alipayClient.execute(request);
//        } catch (AlipayApiException e) {
//            e.printStackTrace();
//        }
//        if (response.isSuccess()) {
//            // 更新交易记录：为关闭状态
//            PaymentInfo paymentInfo = new PaymentInfo();
//            paymentInfo.setPaymentStatus(PaymentStatus.ClOSED.name());
//            paymentService.updatePaymentInfo(orderInfo.getOutTradeNo(), paymentInfo);
//            return true;
//        } else {
//            return false;
//        }
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();

        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no", orderInfo.getOutTradeNo());
        map.put("refund_amount", orderInfo.getTotalAmount());
        map.put("refund_reason", "颜色浅了点");
        // out_request_no

        request.setBizContent(JSON.toJSONString(map));

        try {
            FiltratHttpsUtils.doFiltra();
        } catch (Exception e) {
            e.printStackTrace();
        }
        AlipayTradeRefundResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            // 更新交易记录 ： 关闭
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setPaymentStatus(PaymentStatus.ClOSED.name());
            paymentService.updatePaymentInfo(orderInfo.getOutTradeNo(), paymentInfo);
            return true;
        } else {
            return false;
        }

    }

    @Override
    public Boolean closePay(Long orderId) {
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);
        //AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do","app_id","your private_key","json","GBK","alipay_public_key","RSA2");
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
//        request.setBizContent("{" +
//                "    \"trade_no\":\"2013112611001004680073956707\"," +
//                "    \"out_trade_no\":\"HZ0120131127001\"," +
//                "    \"operator_id\":\"YX01\"" +
//                "  }");
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",orderInfo.getOutTradeNo());
        map.put("operator_id","YX01");
        request.setBizContent(JSON.toJSONString(map));
        AlipayTradeCloseResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            System.out.println("调用成功");
            return true;
        } else {
            System.out.println("调用失败");
            return false;
        }
    }

    @Override
    public Boolean checkPayment(Long orderId) {
//        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do","app_id","your private_key","json","GBK","alipay_public_key","RSA2");
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
//        request.setBizContent("{" +
//                "\"out_trade_no\":\"20150320010101001\"," +
//                "\"trade_no\":\"2014112611001004680 073956707\"," +
//                "\"org_pid\":\"2088101117952222\"," +
//                "      \"query_options\":[" +
//                "        \"TRADE_SETTLE_INFO\"" +
//                "      ]" +
//                "  }");
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",orderInfo.getOutTradeNo());
        request.setBizContent(JSON.toJSONString(map));
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            System.out.println("调用成功");
            return true;
        } else {
            System.out.println("调用失败");
            return false;
        }
    }
}
