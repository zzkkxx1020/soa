package com.atguigu.gmall0319.manage.controller;

import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class FileUploadController {
    // @Value:注解可以从application.properties 配置文件中取得数据
    // fileUrl=http://192.168.67.203
    @Value("${fileServer.url}")
    private String fileUrl;


    // 配置文件http://file.gmall.com=http://192.168.67.203 把ip地址改成软编码
    // 文件上传的时候，参数可以不使用数组，
    // s = M00/00/00/wKhDy1tbPK2APjNCAAJiYjiS9-Y593.jpg  http://192.168.67.203/M00/00/00/wKhDy1tbPK2APjNCAAJiYjiS9-Y593.jpg
    @RequestMapping(value = "fileUpload",method = RequestMethod.POST)
    public String fileUpload(@RequestParam("file") MultipartFile file) throws IOException, MyException {
        String imgUrl=fileUrl;
        if (file!=null){
            // 初始化工作
            String configFile = this.getClass().getResource("/tracker.conf").getFile();
            ClientGlobal.init(configFile);
            TrackerClient trackerClient=new TrackerClient();
            TrackerServer trackerServer=trackerClient.getConnection();
            StorageClient storageClient=new StorageClient(trackerServer,null);
            // 取得文件名称 zly.jpg
            String filename = file.getOriginalFilename();
            // 取得后缀名 .jpg
            String extName = StringUtils.substringAfterLast(filename, ".");
            //   String orginalFilename="e:/img/timg.jpg";
            String[] upload_file = storageClient.upload_file(file.getBytes(), extName, null);
            imgUrl=fileUrl;
            for (int i = 0; i < upload_file.length; i++) {
            //   M00/00/00/wKhDy1tbPK2APjNCAAJiYjiS9-Y593.jpg
                String path = upload_file[i];
                System.out.println("s = " + path);
                imgUrl+="/"+path;
            }
        }
        return imgUrl;
    }

}
