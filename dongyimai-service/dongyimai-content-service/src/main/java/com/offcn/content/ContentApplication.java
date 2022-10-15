package com.offcn.content;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.offcn.content.dao")
public class ContentApplication{
    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class,args);
    }
}
