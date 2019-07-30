package com.atguigu.gmall0319.service;

import com.atguigu.gmall0319.bean.*;

import java.util.List;

public interface ManageService {
    // 查询所有一级分类属性
    List<BaseCatalog1> getCatalog1();
    // 查询二级分类，根据一级分类的id
    List<BaseCatalog2> getCatalog2(String catalog1Id);
    // 查询三级分类，根据二级分类的id
    List<BaseCatalog3> getCatalog3(String catalog2Id);
    // 根据三级分类id查询所有平台属性名称
    List<BaseAttrInfo> getAttrList(String catalog3Id);
    // 大保存平台属性，平台属性值
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);
    // 查询属性信息
    BaseAttrInfo getAttrInfo(String attrId);
    // 根据三级分类查询spuInfo 信息
    List<SpuInfo> getSpuInfoList(SpuInfo spuInfo);
    // 查询所有的销售属性列表
    List<BaseSaleAttr> getBaseSaleAttrList();
    // 保存spuInfo 信息
    void saveSpuInfo(SpuInfo spuInfo);
    // 根据spuId 查询spuImg列表
    List<SpuImage> getSpuImageList(String spuId);

    // 根据商品id查询销售属性
    List<SpuSaleAttr> getSpuSaleAttrList(String spuId);
    // 保存skuInfo 信息
    void saveSkuInfo(SkuInfo skuInfo);
    // 根据skuId查询商品信息
    SkuInfo getSkuInfo(String skuId);
    // 根据skuInfo 信息查询商品的销售属性值
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(SkuInfo skuInfo);
    // 根据spuId 查询销售属性值
    List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);
    // 根据平台属性值的id查询平台属性信息
    List<BaseAttrInfo> getAttrList(List<String> attrValueIdList);
}
