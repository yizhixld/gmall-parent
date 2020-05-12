package com.atguigu.gmall.product.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author yizhixld
 * @create 2020-04-17-19:45
 */
public interface ManageService {
    /**
     * @Description: 查询所有一级分类
     * @Return:
     **/
    List<BaseCategory1> getBaseCategory1();

    /**
     * @Description: 根据一级分类id查询所有二级分类
     * @Return:
     **/
    List<BaseCategory2> getBaseCategory2(Long category1Id);

    /**
     * @Description: 根据二级分类id查询所有三级分类
     * @Return:
     **/
    List<BaseCategory3> getBaseCategory3(Long category2Id);

    /**
     * @Param: [category1Id, category2Id, category3Id]
     * @Description: 查询平台属性
     * @Return:
     * @Create: 2020/4/17
     **/
    List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id);
    /**
    * @Description: 保存平台属性方法
    * @Return:
    **/
    void saveAttrInfo(BaseAttrInfo attrInfo);
    /**
    * @Description: 分页查询商品属性
    * @Return:
    **/
    IPage<SpuInfo> selectPage(Page<SpuInfo> pageParam,SpuInfo spuInfo);
    /**
    * @Description: 根据attrId 去查找AttrInfo
    * @Return:
    **/
    BaseAttrInfo getAttrInfo(Long attrId);
    /**
    * @Description: 获取所有销售属性
    * @Return:
    **/
    List<BaseSaleAttr> getBaseSaleAttrList();
    /**
    * @Description: 保存商品及商品销售属性
    * @Return:
    **/
    void saveSpuInfo(SpuInfo spuInfo);
    /**
    * @Description: 获取商品图片列表信息
    * @Return:
    **/
    List<SpuImage> spuImageList(Long spuId);
    /**
    * @Description: //根据商品spuId获取销售属性及销售属性值
    * @Return:
    **/
    List<SpuSaleAttr> spuSaleAttrList(Long spuId);
    /**
    * @Description: //保存商品信息
    * @Return:
    **/
    void saveSkuInfo(SkuInfo skuInfo);
    /**
    * @Description: //商品信息列表展示
    * @Return:
    **/
    IPage<SkuInfo> selectPage(Page<SkuInfo> skuInfoPage);
    /**
    * @Description: //商品上架
    * @Return:
    **/
    void onSale(Long skuId);
    /**
    * @Description: //商品下架
    * @Return:
    **/
    void cancelSale(Long skuId);
    /**
    * @Description: //查询skuInfo
    * @Return:
    **/
    SkuInfo getSkuInfo(Long skuId);
    /**
    * @Description: //通过三级分类id查询分类信息
    * @Return:
    **/
    BaseCategoryView getCategoryViewByCategory3Id(Long category3Id);
    /**
    * @Description: //获取sku价格
    * @Return:
    **/
    BigDecimal getSkuPrice(Long skuId);
    /**
    * @Description: //根据spuId，skuId 查询销售属性集合
    * @Return:
    **/
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId);
    /**
    * @Description: //根据spuId 查询map 集合属性
    * @Return:
    **/
    Map getSkuValueIdsMap(Long spuId);
    /**
    * @Description: //查询分类信息并在首页展示
    * @Return:
    **/
    List<JSONObject> getBaseCategoryList();
    /**
    * @Description: //通过品牌Id 来查询数据
    * @Return:
    **/
    BaseTrademark getTrademarkByTmId(Long tmId);
    /**
    * @Description: //通过skuId 查询平台属性及属性值
    * @Return:
    **/
    List<BaseAttrInfo> getAttrList(Long skuId);

}
