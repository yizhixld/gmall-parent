package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.model.product.SpuSaleAttrValue;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yizhixld
 * @create 2020-04-20-19:36
 */
@RequestMapping("admin/product")
@RestController
public class SkuManageController {
    @Autowired
    ManageService manageService;

    @ApiOperation(value = "获取商品图片列表")
    @GetMapping("spuImageList/{spuId}")
    public Result spuImageList(@PathVariable Long spuId){
        List<SpuImage> spuImageList =  manageService.spuImageList(spuId);
        return Result.ok(spuImageList);
    }

    @ApiOperation(value = "获取商品销售属性及商品销售属性值")
    @GetMapping("spuSaleAttrList/{spuId}")
    public Result spuSaleAttrList(@PathVariable Long spuId){
       List<SpuSaleAttr> saleAttrList =  manageService.spuSaleAttrList(spuId);
       return Result.ok(saleAttrList);
    }

    @ApiOperation(value = "保存商品信息")
    @PostMapping("saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);
        return Result.ok();
    }

    @ApiOperation(value = "商品信息列表展示")
    @GetMapping("list/{page}/{limit}")
    public Result list(@PathVariable Long page, @PathVariable Long limit){
        Page<SkuInfo> skuInfoPage = new Page<>(page, limit);
        IPage<SkuInfo> skuInfoIPage = manageService.selectPage(skuInfoPage);
        return Result.ok(skuInfoIPage);
    }

    @ApiOperation("商品上架")
    @GetMapping("onSale/{skuId}")
    public Result onSale(@PathVariable Long skuId){
        manageService.onSale(skuId);
        return Result.ok();
    }

    @ApiOperation("商品下架")
    @GetMapping("cancelSale/{skuId}")
    public Result cancelSale(@PathVariable Long skuId){
        manageService.cancelSale(skuId);
        return Result.ok();
    }

}
