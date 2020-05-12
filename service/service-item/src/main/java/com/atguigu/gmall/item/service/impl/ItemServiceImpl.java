package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.item.config.ThreadPoolConfig;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author yizhixld
 * @create 2020-04-21-20:27
 */
@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private ListFeignClient listFeignClient;

    @Override
    public Map<String, Object> getBySkuId(Long skuId) {
        Map<String,Object> resultMap = new HashMap<>();
        CompletableFuture<SkuInfo> skuCompletableFuture  = CompletableFuture.supplyAsync(() -> {
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            resultMap.put("skuInfo", skuInfo);
            return skuInfo;
        },threadPoolExecutor);

        CompletableFuture<Void> categoryViewCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfo -> {
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            resultMap.put("categoryView", categoryView);
        }, threadPoolExecutor);

        CompletableFuture<Void> priceCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfo -> {
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuInfo.getId());
            resultMap.put("price", skuPrice);
        }, threadPoolExecutor);

        CompletableFuture<Void> valuesSkuJsonCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfo -> {
            Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
            String valuesSkuJson = JSON.toJSONString(skuValueIdsMap);
            resultMap.put("valuesSkuJson", valuesSkuJson);
        }, threadPoolExecutor);

        CompletableFuture<Void> spuSaleAttrListCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfo -> {
            List<SpuSaleAttr> spuSaleAttrListCheckBySku = productFeignClient.getSpuSaleAttrListCheckBySku(skuInfo.getId(), skuInfo.getSpuId());
            resultMap.put("spuSaleAttrList", spuSaleAttrListCheckBySku);
        }, threadPoolExecutor);

        CompletableFuture<Void> incrHotScoreCompletableFuture = CompletableFuture.runAsync(() -> {
            listFeignClient.incrHotScore(skuId);
        }, threadPoolExecutor);

        CompletableFuture.allOf(skuCompletableFuture,categoryViewCompletableFuture,priceCompletableFuture,valuesSkuJsonCompletableFuture,spuSaleAttrListCompletableFuture,incrHotScoreCompletableFuture).join();


//        //远程调用，封装map，现获取skuInfo
//        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
//        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
//        BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
//        Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
//        List<SpuSaleAttr> spuSaleAttrListCheckBySku = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
//
//        //封装map
//        resultMap.put("skuInfo",skuInfo);
//        resultMap.put("price",skuPrice);
//        resultMap.put("categoryView",categoryView);
//        resultMap.put("spuSaleAttrList",spuSaleAttrListCheckBySku);
//
//        String valuesSkuJson  = JSON.toJSONString(skuValueIdsMap);
//        resultMap.put("valuesSkuJson",valuesSkuJson);

        return resultMap;
    }


}
