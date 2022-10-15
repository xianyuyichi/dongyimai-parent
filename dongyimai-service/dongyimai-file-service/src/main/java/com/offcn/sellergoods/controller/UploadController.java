package com.offcn.sellergoods.controller;

import com.offcn.sellergoods.entity.Result;
import com.offcn.sellergoods.util.FastDFSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {

    @Value("${FILE_SERVER_URL}")
    private String url;

    /**
     * 图片上传
     * @param file
     * @return
     */
    @RequestMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file){
        try {
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:fdfs_client.conf");
            String path = fastDFSClient.uploadFile(file.getBytes(), "jpg");
            String tempUrl = url;
            tempUrl = tempUrl + path;
            return new Result(tempUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail();
        }
    }
}
