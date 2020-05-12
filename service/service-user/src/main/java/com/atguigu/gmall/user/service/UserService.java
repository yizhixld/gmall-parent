package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserInfo;

/**
 * @author yizhixld
 * @create 2020-04-29-22:22
 */
public interface UserService {
    /**
    * @Description: //用户登录
    * @Return:
    **/
    UserInfo login(UserInfo userInfo);
}
