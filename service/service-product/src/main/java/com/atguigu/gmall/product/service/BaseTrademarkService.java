package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author yizhixld
 * @create 2020-04-19-15:49
 */
public interface BaseTrademarkService extends IService<BaseTrademark> {
    IPage<BaseTrademark> selectPage(Page<BaseTrademark> trademarkPage);
}
