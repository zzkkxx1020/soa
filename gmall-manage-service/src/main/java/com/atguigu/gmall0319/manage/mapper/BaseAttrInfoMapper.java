package com.atguigu.gmall0319.manage.mapper;

import com.atguigu.gmall0319.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {
    // 根据三级分类id查找平台属性-- 通用mapper 针对的都是简单的查询。
    List<BaseAttrInfo> getBaseAttrInfoListByCatalog3Id(Long catalog3Id);
    // mybatis 传递数据的时候，@Param()给参数指定一个具体的名称。
    List<BaseAttrInfo> selectAttrInfoListByIds(@Param(value = "valueIds") String valueIds);
}
