package com.atguigu.gmall0319.bean;

import java.io.Serializable;
import java.util.List;

public class SkuLsResult implements Serializable {

//    从es中取得的结果集
    List<SkuLsInfo> skuLsInfoList;
//     从es中查询到的所有结果数
    long total;
//  总页数
    long totalPages;
//  返回的平台属性值id的集合
    List<String> attrValueIdList;

    public List<SkuLsInfo> getSkuLsInfoList() {
        return skuLsInfoList;
    }

    public void setSkuLsInfoList(List<SkuLsInfo> skuLsInfoList) {
        this.skuLsInfoList = skuLsInfoList;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(long totalPages) {
        this.totalPages = totalPages;
    }

    public List<String> getAttrValueIdList() {
        return attrValueIdList;
    }

    public void setAttrValueIdList(List<String> attrValueIdList) {
        this.attrValueIdList = attrValueIdList;
    }
}
