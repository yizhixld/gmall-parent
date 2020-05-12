package com.atguigu.gmall.list.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.client.impl.ListFeignClientImpl;
import com.atguigu.gmall.model.list.SearchParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @author yizhixld
 * @create 2020-04-27-22:04
 */
@FeignClient(name = "service-list",fallback = ListFeignClientImpl.class)
public interface ListFeignClient {

    @PostMapping("/api/list")
    public Result list(@RequestBody SearchParam searchParam);

    @GetMapping("api/list/inner/upperGoods/{skuId}")
    public Result upperGoods(@PathVariable Long skuId);

    @GetMapping("api/list/inner/lowerGoods/{skuId}")
    public Result lowerGoods(@PathVariable Long skuId);

    @GetMapping("api/list/inner/incrHotScore/{skuId}")
    Result incrHotScore(@PathVariable Long skuId);
}
