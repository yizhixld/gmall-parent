package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author yizhixld
 * @create 2020-04-17-14:14
 */
@Mapper
public interface BaseAttrInfoMapper extends BaseMapper<BaseAttrInfo> {
    /**
    * @Description: 根据分类Id 查询平台属性集合对象
    * @Return:
    **/
    List<BaseAttrInfo> selectBaseAttrInfoList(@Param("category1Id") Long category1Id,
                                              @Param("category2Id") Long category2Id,
                                              @Param("category3Id") Long category3Id);

    /**
    * @Description: //通过skuId 查询平台属性及属性值
    * @Return:
    **/
    List<BaseAttrInfo> selectBaseAttrInfoListBySkuId(Long skuId);
}
