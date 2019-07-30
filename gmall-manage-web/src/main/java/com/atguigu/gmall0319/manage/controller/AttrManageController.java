package com.atguigu.gmall0319.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0319.bean.*;
import com.atguigu.gmall0319.service.ListService;
import com.atguigu.gmall0319.service.ManageService;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.midi.Soundbank;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Controller
public class AttrManageController {

    // 数据展示 ，获得到gmall-manage-service中的服务
    @Reference
    private ManageService manageService;

    @Reference
    private ListService listService;

    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<BaseCatalog1> getCatalog1(){
       return manageService.getCatalog1();
    }


    @RequestMapping("getCatalog2")
    @ResponseBody
    public List<BaseCatalog2> getCatalog2(String catalog1Id){
        return manageService.getCatalog2(catalog1Id);
    }

    @RequestMapping("getCatalog3")
    @ResponseBody
    public List<BaseCatalog3> getCatalog3(String catalog2Id){
        return manageService.getCatalog3(catalog2Id);
    }

    @RequestMapping("attrInfoList")
    @ResponseBody
    public List<BaseAttrInfo> getAttrInfoList(String catalog3Id){
        return manageService.getAttrList(catalog3Id);
    }


    @RequestMapping(value = "saveAttrInfo",method = RequestMethod.POST)
    @ResponseBody
    public String saveAttrInfo(BaseAttrInfo baseAttrInfo){
        // 从页面传递过来
        manageService.saveAttrInfo(baseAttrInfo);
        return "success";
    }

    @RequestMapping("getAttrValueList")
    @ResponseBody
    public List<BaseAttrValue> getAttrValueList(String attrId){
        // 属性---属性值 是一一对应
      BaseAttrInfo baseAttrInfo =  manageService.getAttrInfo(attrId);
      return baseAttrInfo.getAttrValueList();
    }

    //    商品上架控制器，根据skuId 查询出skuInfo 然后将skuInfo的属性赋给skuLsInfo
    @RequestMapping("onSale")
    @ResponseBody
    public void  onSale(String skuId){
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
//      进行属性拷贝
        SkuLsInfo skuLsInfo = new SkuLsInfo();

//        skuLsInfo.setId(skuInfo.getId());
//        skuLsInfo.setCatalog3Id(skuInfo.getCatalog3Id());
//        使用工具类 第一个参数是目标对象，第二个是源对象
        try {
            BeanUtils.copyProperties(skuLsInfo,skuInfo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
//      保存到es中
        listService.saveSkuInfo(skuLsInfo);
    }

}
