package com.atguigu.gmall.item.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.impl.ItemFeignClientImpl;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author yizhixld
 * @create 2020-04-22-22:31
 */
@FeignClient(value = "service-item",fallback = ItemFeignClientImpl.class)
public interface ItemFeignClient {
    @GetMapping("api/item/{skuId}")
    public Result getItem(@PathVariable Long skuId);
}
