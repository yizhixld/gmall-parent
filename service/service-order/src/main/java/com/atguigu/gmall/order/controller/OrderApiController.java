package com.atguigu.gmall.order.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yizhixld
 * @create 2020-05-04-20:42
 */
@RestController
@RequestMapping("api/order")
public class OrderApiController {
    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private CartFeignClient cartFeignClient;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private OrderService orderService;

    @GetMapping("auth/trade")
    public Result<Map<String, Object>> trade(HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        // 获取用户地址
        List<UserAddress> addressList = userFeignClient.findUserAddressListByUserId(Long.valueOf(userId));
        // 渲染送货清单
        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList(userId);
        // 声明一个集合存储订单明细
        ArrayList<OrderDetail> orderDetailList = new ArrayList<>();
        int totalNum = 0 ;
        for (CartInfo cartInfo : cartCheckedList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetailList.add(orderDetail);
            totalNum += cartInfo.getSkuNum();
        }
        // 计算总金额
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        orderInfo.sumTotalAmount();
        // 获取流水号
        String tradeNo = orderService.getTradeNo(userId);

        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("userAddressList",addressList);
        resultMap.put("detailArrayList",orderDetailList);
        resultMap.put("totalNum",totalNum);
        resultMap.put("totalAmount", orderInfo.getTotalAmount());
        resultMap.put("tradeNo",tradeNo);
        return Result.ok(resultMap);
    }

    @PostMapping("auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo, HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        orderInfo.setUserId(Long.parseLong(userId));
        // 下单之前做校验
        String tradeNo = request.getParameter("tradeNo");
        if(!orderService.checkTradeCode(userId,tradeNo)){
            return Result.fail().message("不能重复提交订单！");
        }
        // 删除订单号
        orderService.deleteTradeNo(userId);
        // 验证库存
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            boolean result = orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
            if(!result){
                return Result.fail().message(orderDetail.getSkuName() + "库存不足！");
            }
            // 验证价格
            BigDecimal skuPrice = productFeignClient.getSkuPrice(orderDetail.getSkuId());
            if(skuPrice.compareTo(orderDetail.getOrderPrice()) != 0){
               // 重新查询价格
                cartFeignClient.loadCartCache(userId);
                return Result.fail().message(orderDetail.getSkuName() + "价格有变！");
            }
        }
        // 验证通过，保存订单！
        Long orderId = orderService.saveOrderInfo(orderInfo);
        return Result.ok(orderId);
    }

    @GetMapping("inner/getOrderInfo/{orderId}")
    public OrderInfo getOrderInfo(@PathVariable Long orderId){
        return orderService.getOrderInfo(orderId);
    }

    @RequestMapping("orderSplit")
    public String orderSplit(HttpServletRequest request){
        String orderId = request.getParameter("orderId");
        String wareSkuMap = request.getParameter("wareSkuMap");
        // 拆单：获得子订单集合
        List<OrderInfo> subOrderInfoList = orderService.orderSplit(Long.parseLong(orderId),wareSkuMap);
        List<Map> mapArrayList = new ArrayList<>();
        // 生成子订单集合
        for (OrderInfo orderInfo : subOrderInfoList) {
            Map map = orderService.initWareOrder(orderInfo);
            mapArrayList.add(map);
        }
        return JSON.toJSONString(mapArrayList);
    }

    @PostMapping("inner/seckill/submitOrder")
    public Long submitOrder(@RequestBody OrderInfo orderInfo){
        Long orderId = orderService.saveOrderInfo(orderInfo);
        return orderId;
    }
}

