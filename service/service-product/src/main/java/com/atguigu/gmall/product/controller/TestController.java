package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yizhixld
 * @create 2020-04-23-10:39
 */
@RestController
@RequestMapping("admin/product/test")
public class TestController {
    @Autowired
    TestService testService;

    @GetMapping("testLock")
    public Result testLock(){
        testService.testLock();
        return Result.ok();
    }

}
