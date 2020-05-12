package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author yizhixld
 * @create 2020-04-29-22:29
 */
@RestController
@RequestMapping("/api/user/passport")
public class PassportController {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserInfoMapper userInfoMapper;

    @ApiOperation(value = "登录")
    @PostMapping("login")
    public Result login(@RequestBody UserInfo userInfo){
        UserInfo loginUser = userService.login(userInfo);
        if(null != loginUser){
            //设置token
            String token = UUID.randomUUID().toString().replaceAll("-","");
            HashMap<String,Object> map = new HashMap<>();
            map.put("token",token);
            map.put("name",loginUser.getName());
            map.put("nickName",loginUser.getNickName());
            //设置缓存中存放的key名称(user:info:+token)
            String userKey = RedisConst.USER_LOGIN_KEY_PREFIX + token;
            redisTemplate.opsForValue().set(userKey,loginUser.getId().toString(),RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);
            return Result.ok(map);
        }else {
            return Result.fail().message("用户名或密码错误");
        }
    }
    @ApiOperation(value = "退出登录")
    @GetMapping("logout")
    public Result logout(HttpServletRequest request){
        String token = request.getHeader("token");
        redisTemplate.delete(RedisConst.USER_LOGIN_KEY_PREFIX+token);
        return Result.ok();
    }

    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id){
        UserInfo info = userInfoMapper.selectById(id);
        return Result.ok(info);
    }

    @PostMapping("post")
    public Result post(@RequestBody UserInfo userInfo){
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",userInfo.getId());
        UserInfo info = userInfoMapper.selectOne(queryWrapper);
        return Result.ok(info);
    }

}
