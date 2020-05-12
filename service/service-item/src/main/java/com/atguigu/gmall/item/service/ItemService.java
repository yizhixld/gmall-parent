package com.atguigu.gmall.item.service;

import java.util.Map;

/**
 * @author yizhixld
 * @create 2020-04-21-20:24
 */
public interface ItemService {
    /**
    * @Description: //根据商品id获得商品信息的展示
    * @Return:
    **/
    Map<String,Object> getBySkuId(Long skuId);
}
