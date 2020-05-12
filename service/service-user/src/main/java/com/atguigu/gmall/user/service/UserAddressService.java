package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserAddress;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author yizhixld
 * @create 2020-05-04-19:50
 */
public interface UserAddressService extends IService<UserAddress> {
    /**
    * @Description: //根据用户Id 查询用户的收货地址列表！
    * @Return:
    **/
    List<UserAddress> findUserAddressListByUserId(Long userId);
}
