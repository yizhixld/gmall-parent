package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yizhixld
 * @create 2020-04-19-15:52
 */
@RestController
@RequestMapping("/admin/product/baseTrademark")
public class BaseTrademarkController {
    @Autowired
    BaseTrademarkService baseTrademarkService;

    @ApiOperation(value = "品牌分页查询" )
    @GetMapping("{page}/{size}")
    public Result index(@PathVariable Long page,@PathVariable Long size ){
        Page<BaseTrademark> trademarkPage = new Page<>(page, size);
        IPage<BaseTrademark> pageModel = baseTrademarkService.selectPage(trademarkPage);
        return Result.ok(pageModel);
    }

    @ApiOperation(value = "查找所有品牌")
    @GetMapping("getTrademarkList")
    public Result getTrademarkList(){
        List<BaseTrademark> trademarkList = baseTrademarkService.list(null);
        return Result.ok(trademarkList);
    }

    @ApiOperation(value = "根据id获得品牌")
    @GetMapping("get/{id}")
    public Result getById(@PathVariable Long id){
        BaseTrademark baseTrademark = baseTrademarkService.getById(id);
        return Result.ok(baseTrademark);
    }

    @ApiOperation(value = "保存品牌信息")
    @PostMapping("save")
    public Result save(@RequestBody BaseTrademark baseTrademark){
        baseTrademarkService.save(baseTrademark);
        return Result.ok();
    }

    @ApiOperation(value = "根据id更新品牌信息")
    @PutMapping("update")
    public Result updateById(@RequestBody BaseTrademark baseTrademark){
        baseTrademarkService.updateById(baseTrademark);
        return Result.ok();
    }

    @ApiOperation(value = "根据id移除品牌信息")
    @DeleteMapping("remove/{id}")
    public Result removeById(@PathVariable Long id){
        baseTrademarkService.removeById(id);
        return Result.ok();
    }

}
