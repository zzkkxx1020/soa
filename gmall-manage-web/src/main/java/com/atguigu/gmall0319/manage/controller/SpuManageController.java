package com.atguigu.gmall0319.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0319.bean.BaseSaleAttr;
import com.atguigu.gmall0319.bean.SpuInfo;
import com.atguigu.gmall0319.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class SpuManageController {

    @Reference
    private ManageService manageService;

    @RequestMapping("spuListPage")
    public String spuListPage(){
        return "spuListPage";
    }

//    http://localhost:8082/spuList?catalog3Id=61 根据三级分类Id，进行查询所有spuInfo信息
    @RequestMapping("spuList")
    @ResponseBody
    public List<SpuInfo> spuList(String catalog3Id){
    // 调用服务层 catalog3Id=封装到spuInfo 中
        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);
        List<SpuInfo> spuInfoList = manageService.getSpuInfoList(spuInfo);
        return spuInfoList;
    }
    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<BaseSaleAttr> getBaseSaleAttrList(){
        return manageService.getBaseSaleAttrList();
    }

    // String success void ,int ,boolean 。
    @RequestMapping(value = "saveSpuInfo",method = RequestMethod.POST)
    @ResponseBody
    public String saveSpuInfo(SpuInfo spuInfo){
        // 调用服务层
        manageService.saveSpuInfo(spuInfo);
        return "success";
    }
}
