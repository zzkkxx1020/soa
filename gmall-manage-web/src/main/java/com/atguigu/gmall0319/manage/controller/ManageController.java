package com.atguigu.gmall0319.manage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ManageController {

    @RequestMapping("index")
    public String index(){
    //  视图 spring boot 默认返回thymeleaf 页面
        return "index";
    }

    // 左侧菜单栏中平台属性控制器
    @RequestMapping("attrListPage")
    public String getAttrListPage(){
        /*attrListPage.html*/
        return "attrListPage";
    }



}
