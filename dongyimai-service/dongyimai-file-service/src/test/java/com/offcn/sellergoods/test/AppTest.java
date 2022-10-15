package com.offcn.sellergoods.test;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class AppTest {

    @Test
    public void upload() throws UnsupportedEncodingException {
        /*String path = this.getClass().getResource("/").getPath();
        String decode = URLDecoder.decode(path, "utf-8");
        System.out.println(decode);*/
        try {
            ClientGlobal.init("D:\\2022成都系统\\code\\dongyimai-parent\\dongyimai-service\\dongyimai-file-service\\src\\main\\resources\\fdfs_client.conf");
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getConnection();
            //创建Storage 客户端对象
            StorageClient storageClient = new StorageClient(trackerServer, null);
            //args1:上传的文件的本地路径; args2:上传的文件扩展名字
            String[] strings = storageClient.upload_file("C:\\Users\\14629\\Pictures\\xhx.jpg", "jpg", null);
            System.out.println(Arrays.toString(strings));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
    }
}
