package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yizhixld
 * @create 2020-04-17-20:48
 */
@Api(tags = "商品基础属性接口")
@RestController
@RequestMapping("admin/product")
public class BaseManageController {
    @Autowired
    ManageService manageService;

    @GetMapping("getCategory1")
    public Result getBaseCategory1(){
        List<BaseCategory1> baseCategory1List = manageService.getBaseCategory1();
        return Result.ok(baseCategory1List);
    }

    @GetMapping("getCategory2/{category1Id}")
    public Result getBaseCategory2(@PathVariable Long category1Id){
        List<BaseCategory2> baseCategory2List = manageService.getBaseCategory2(category1Id);
        return Result.ok(baseCategory2List);
    }

    @GetMapping("getCategory3/{category2Id}")
    public Result getBaseCategory3(@PathVariable Long category2Id){
        List<BaseCategory3> baseCategory3List = manageService.getBaseCategory3(category2Id);
        return Result.ok(baseCategory3List);
    }

    @GetMapping("attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result getAttrInfoList(@PathVariable Long category1Id,@PathVariable Long category2Id,@PathVariable Long category3Id){
        List<BaseAttrInfo> attrInfoList = manageService.getAttrInfoList(category1Id, category2Id, category3Id);
        return Result.ok(attrInfoList);
    }

    @PostMapping("saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        manageService.saveAttrInfo(baseAttrInfo);
        return Result.ok();
    }

    @GetMapping("getAttrValueList/{attrId}")
    public Result getAttrValueList(@PathVariable Long attrId){
        BaseAttrInfo attrInfo = manageService.getAttrInfo(attrId);
        List<BaseAttrValue> attrValueList = attrInfo.getAttrValueList();
        return Result.ok(attrValueList);
    }

    @GetMapping("{page}/{size}")
    public Result index(@PathVariable Long page,
                        @PathVariable Long size,
                        SpuInfo spuInfo){
        Page<SpuInfo> attrInfoPage = new Page<>(page, size);
        IPage<SpuInfo> spuInfoIPage = manageService.selectPage(attrInfoPage, spuInfo);
        return Result.ok(spuInfoIPage);
    }

    @PostMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo){
        manageService.saveSpuInfo(spuInfo);
        return Result.ok();
    }

}
