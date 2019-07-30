package com.atguigu.gmall0319.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0319.bean.*;
import com.atguigu.gmall0319.service.ListService;
import com.atguigu.gmall0319.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.crypto.interfaces.PBEKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    @Reference
    private ListService listService;

    @Reference
    private ManageService manageService;

    @RequestMapping("list.html")
    public String list(SkuLsParams skuLsParams, Model model){
        // 设置每页显示的条数
        skuLsParams.setPageSize(2);

        // 调用listService
        SkuLsResult skuLsResult = listService.search(skuLsParams);
        // 将对象转成json字符串
        String listJson = JSON.toJSONString(skuLsResult);
        System.out.println(listJson);
//       应该在页面显示的是skuLsInfo属性的信息
        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();
//       从es中查询出来的skuInfo 信息，页面在页面显示
        model.addAttribute("skuLsInfoList",skuLsInfoList);
//        返回值的时候，有了平台属性值集合
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
//        根据平台属性值id进行查询平台属性信息。baseAttrInfo  private List<BaseAttrValue> attrValueList;
//        在后台写一个方法，根据平台属性值的集合查询平台属性信息
        List<BaseAttrInfo> attrList = manageService.getAttrList(attrValueIdList);
//        保存平台属性集合信息,从平台属性中获取平台属性值
        model.addAttribute("attrList",attrList);
//        点击平台属性值的时候，将url添加上平台属性值的参数 看作一个字段 http://list.gmall.com/list.html?catalog3Id=61
//        http://list.gmall.com/list.html?catalog3Id=61&valueId=80&valueId=83
//        面包屑：平台属性：平台属性值：
//        选择平台属性值，相当于添加了一个过滤条件，
//        将面包屑放到一个集合列表中
        List<BaseAttrValue> baseAttrValuesList = new ArrayList<>();
//        BaseAttrValue.setvalueName(“平台属性：平台属性值”)

//        拼接完之后，http://list.gmall.com/list.html?keyword="小米"&catalog3Id=61&valaueId=80&valueId=83
//        makeUrlParam(skuLsParams,String ... excludeValueIds)
        String urlParam = makeUrlParam(skuLsParams);
//      去重 iter foreach ,itar array ,itco:Iterator
        for (Iterator<BaseAttrInfo> iterator = attrList.iterator(); iterator.hasNext(); ) {
//            平台属性
            BaseAttrInfo baseAttrInfo =  iterator.next();
            // 取得数据库中平台属性值Id
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            /*数据库的Id，跟skuLsParam 做比较，如果有相同的，则移除*/
            for (BaseAttrValue baseAttrValue : attrValueList) {
//                从新赋值一个urlParam
                baseAttrValue.setUrlParam(urlParam);
                if (skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){
//                    循环做匹配，移除数据
                    for (String valueId : skuLsParams.getValueId()) {
                        if (baseAttrValue.getId().equals(valueId)){
//                            如果平台属性值Id相同则将数据移除
                            iterator.remove();
//                            该循环中有平台属性和平台属性值。创建一个被选中的平台属性值对象
                            BaseAttrValue baseAttrValueselected = new BaseAttrValue();
                            baseAttrValueselected.setValueName(baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName());
//                           做去除重复操作 skuLsParams有可能是多个值，valueId
                            String makeUrlParam = makeUrlParam(skuLsParams, valueId);
                            baseAttrValueselected.setUrlParam(makeUrlParam);
//                          添加到面包屑集合中
                            baseAttrValuesList.add(baseAttrValueselected);
                        }
                    }
                }
            }
        }
//        关键字
        model.addAttribute("keyword",skuLsParams.getKeyword());
        model.addAttribute("baseAttrValuesList",baseAttrValuesList);
        // 保存urlParam
        model.addAttribute("urlParam",urlParam);
        // 保存一下totalPages，pageNo 总条数是通过es
//        int pages = (int) ((skuLsResult.getTotal() +(skuLsParams.getPageSize()-1))/skuLsParams.getPageSize());
        model.addAttribute("totalPages",skuLsResult.getTotalPages());
        model.addAttribute("pageNo",skuLsParams.getPageNo());
        return "list";
    }

    // 制作url参数 SkuLsParams
    /**
     *
     * @param skuLsParams url：中的参数
     * @param excludeValueIds 通过点击平台属性值【点击面包屑的时候传入的平台属性值Id】
     * @return
     */
    public String makeUrlParam(SkuLsParams skuLsParams,String ... excludeValueIds){
//        先声明一个参数
        String urlParam="";
//        当keyword 不为空的时候，拼接参数 http://list.gmall.com/list.html?keyword="小米"
        if (skuLsParams.getKeyword()!=null && skuLsParams.getKeyword().length()>0){
            urlParam+="keyword="+skuLsParams.getKeyword();
        }
//        拼接三级分类Id  当keyword 不为空的时候，拼接参数 http://list.gmall.com/list.html?keyword="小米"&catalog3Id=61
        if (skuLsParams.getCatalog3Id()!=null && skuLsParams.getCatalog3Id().length()>0){
            if (urlParam.length()>0){
                urlParam+="&";
            }
            urlParam+="catalog3Id="+skuLsParams.getCatalog3Id();
        }
//      拼接属性值Id 拼接参数 http://list.gmall.com/list.html?keyword="小米"&catalog3Id=61&valaueId=80&valueId=83
        if (skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){
            for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                String valueId  = skuLsParams.getValueId()[i];
//               判断两个平台属性值的Id是否相等
                if (excludeValueIds!=null && excludeValueIds.length>0){
                    // 只取得当前的第一个值
                    String excludeValueId = excludeValueIds[0];
//                    for (String excludeValueId : excludeValueIds) {
                        if (excludeValueId.equals(valueId)){
                            // 此平台属性值的id则不拼接
                            continue;
                        }
//                    }
                }
                if (urlParam.length()>0){
                    urlParam+="&";
                }
                urlParam+="valueId="+valueId;
            }
        }
        return urlParam;
    }
}
