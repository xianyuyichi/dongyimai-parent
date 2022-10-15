package com.offcn.sellergoods.utils;

import com.alibaba.fastjson.JSON;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;

public class TokenDecode {
    //公钥
    private static final String PUBLIC_KEY = "public.key";
    //公钥的内容
    private static String publickey="";

    /**
     * 获取公钥内容
     *
     */
    public String getPubKey(){
        //判断公钥内容是否为空，不为空直接返回
        if(!StringUtils.isEmpty(publickey)){
            return publickey;
        }
        try {
            //加载类路径下的公钥文件
            Resource resource= new ClassPathResource(PUBLIC_KEY);
            InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            publickey= bufferedReader.lines().collect(Collectors.joining("\n"));
            return publickey;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //解析令牌数据
    public Map<String,String> dcodeToken(String token){
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(getPubKey()));
        //获取原始内容
        String claims = jwt.getClaims();
        //解析json字符串为对象
        return JSON.parseObject(claims, Map.class);
    }

    //从容器获取令牌，并解析
    public Map<String,String> getUserInfo(){
        //获取授权信息
        OAuth2AuthenticationDetails details= (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();

        //令牌解析
        return dcodeToken(details.getTokenValue());
    }
}
