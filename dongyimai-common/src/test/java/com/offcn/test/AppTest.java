package com.offcn.test;

import io.jsonwebtoken.*;
import org.apache.commons.collections.map.HashedMap;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;
import java.util.Map;

public class AppTest {

    @Test
    public void jwt(){
        Map<String,Object> map = new HashedMap();
        map.put("username","xiaozhang");
        map.put("role","admin");
        JwtBuilder builder= Jwts.builder()
                .setId("888")//设置唯一编号
                .setSubject("小白")//设置主题可以是JSON数据
                .setIssuedAt(new Date())//设置签发日期
                //.setExpiration(new Date())
                .addClaims(map)
                .signWith(SignatureAlgorithm.HS256,"ujiuye");//设置签名使用HS256算法，并设置SecretKey(字符串)
        //构建并返回一个字符串
        System.out.println( builder.compact());
    }

    @Test
    public void jwt2(){
        String str = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI4ODgiLCJzdWIiOiLlsI_nmb0iLCJpYXQiOjE2NTgyODAxMjR9.Q0TpWQSowWDhp0P-QH3phxWeHtksL2UY6k2kJUQamTM";
        String str2 = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI4ODgiLCJzdWIiOiLlsI_nmb0iLCJpYXQiOjE2NTgyODA0OTYsImV4cCI6MTY1ODI4MDQ5Nn0.B5ddEafmP8aqHeLvv6-VtPpZifuYOUTQb5zAfVae77Y";

        String str3 = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI4ODgiLCJzdWIiOiLlsI_nmb0iLCJpYXQiOjE2NTgyODA3MzgsInVzZXJuYW1lIjoieGlhb3poYW5nIiwicm9sZSI6ImFkbWluIn0.HsDCr4HegKWWQV8CqgvZn2T5fHy1pS9PU0h1dW_f2ek";

        JwtParser parser = Jwts.parser();
        parser.setSigningKey("ujiuye");
        Jwt jwt = parser.parse(str3);
        System.out.println(jwt);
    }

    @Test
    public void pwd(){
        String password="ujiuye";
        System.out.println(new BCryptPasswordEncoder().encode(password));
    }
}
