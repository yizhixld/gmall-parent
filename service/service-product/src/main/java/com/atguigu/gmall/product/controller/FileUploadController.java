package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import org.apache.commons.io.FilenameUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author yizhixld
 * @create 2020-04-19-16:31
 */
@RestController
@RequestMapping("admin/product")
public class FileUploadController {

    @Value("${fileServer.url}")
    private String fileUrl;

    @PostMapping("fileUpload")
    public Result fileUpload(MultipartFile file) throws IOException, MyException {
        // 获取resource 目录下的tracker.conf 注意：项目目录中千万不能有中文！
        String configFile = this.getClass().getResource("/tracker.conf").getFile();
        // 什么图片返回的路径
        String path = null;
        if (configFile != null) {
            // 初始化文件
            ClientGlobal.init(configFile);
            // 文件上传 需要tracker,storage
            TrackerClient trackerClient = new TrackerClient();
            // 获取trackerServer
            TrackerServer trackerServer = trackerClient.getConnection();
            // 获取storageClient
            StorageClient1 storageClient = new StorageClient1(trackerServer, null);
            // 上传文件
            // 第一个参数表示要上传文件的字节数组
            // 第二个参数：文件的后缀名
            // 第三个参数： 数组，null
            path = storageClient.upload_appender_file1(file.getBytes(), FilenameUtils.getExtension(file.getOriginalFilename()),null);
            // 上传完成之后，需要获取到文件的上传路径
            System.out.println("图片路径:" + fileUrl + path);
        }
        return Result.ok(fileUrl+path);
    }
}
