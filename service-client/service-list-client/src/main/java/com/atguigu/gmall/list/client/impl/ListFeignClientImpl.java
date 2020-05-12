package com.atguigu.gmall.list.client.impl;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.SearchParam;
import org.springframework.stereotype.Component;

/**
 * @author yizhixld
 * @create 2020-04-27-22:06
 */
@Component
public class ListFeignClientImpl implements ListFeignClient {
    @Override
    public Result list(SearchParam searchParam) {
        return null;
    }

    @Override
    public Result upperGoods(Long skuId) {
        return null;
    }

    @Override
    public Result lowerGoods(Long skuId) {
        return null;
    }

    @Override
    public Result incrHotScore(Long skuId) {
        return null;
    }
}
