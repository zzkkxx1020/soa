package com.atguigu.gmall0319.bean;

import java.io.Serializable;
// 封装es中的过滤平台属性的实体类
public class SkuLsAttrValue implements Serializable{

//    平台属性值的Id
    private String valueId;

    public String getValueId() {
        return valueId;
    }

    public void setValueId(String valueId) {
        this.valueId = valueId;
    }
}
