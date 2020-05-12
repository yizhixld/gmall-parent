package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.service.UserAddressService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yizhixld
 * @create 2020-05-04-19:53
 */
@Service
public class UserAddressServiceImpl  extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService  {
    @Autowired
    private UserAddressMapper userAddressMapper;
    @Override
    public List<UserAddress> findUserAddressListByUserId(Long userId) {
        QueryWrapper<UserAddress> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        List<UserAddress> userAddressesList = userAddressMapper.selectList(queryWrapper);
        return userAddressesList;
    }
}
