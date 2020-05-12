package com.atguigu.gmall.list.service;

import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;

import java.io.IOException;

/**
 * @author yizhixld
 * @create 2020-04-27-21:08
 */
public interface SearchService {

    /**
    * @Description: //上架商品列表
    * @Return:
    **/
    void upperGoods(Long skuId);

    /**
    * @Description: //下架商品列表
    * @Return:
    **/
    void lowerGoods(Long skuId);
    /**
    * @Description: //更新热点
    * @Return:
    **/
    void incrHotScore(Long skuId);
    /**
    * @Description: //根据搜索信息封装商品列表
    * @Return:
    **/
    SearchResponseVo search(SearchParam searchParam) throws IOException;
}
