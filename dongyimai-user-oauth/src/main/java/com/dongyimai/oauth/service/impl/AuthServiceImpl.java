package com.dongyimai.oauth.service.impl;

import com.dongyimai.oauth.service.AuthService;
import com.dongyimai.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private DiscoveryClient discoveryClient;

    @Override
    public AuthToken login(String username, String password, String clientId, String clientSecret) {
        AuthToken authToken = new AuthToken();
        //获取token
        List<ServiceInstance> instances = discoveryClient.getInstances("user-auth");
        if(!CollectionUtils.isEmpty(instances)){
            ServiceInstance serviceInstance = instances.get(0);
            String host = serviceInstance.getHost();
            int port = serviceInstance.getPort();
            String url = "http://"+host+":"+port+"/oauth/token";

            //请求体信息
            MultiValueMap<String,String> bodyMap = new LinkedMultiValueMap<>();
          /*  bodyMap.put("grant_type", Arrays.asList("password"));
            bodyMap.put("username",Arrays.asList(username));*/
            bodyMap.add("grant_type","password");
            bodyMap.add("username",username);
            bodyMap.add("password",password);

            //请求头信息
            MultiValueMap<String,String> headerMap = new LinkedMultiValueMap<>();
            headerMap.add("Authorization",this.httpbasic(clientId,clientSecret)); //bearer id,密钥

            //指定 restTemplate当遇到400或401响应时候也不要抛出异常，也要正常返回值
            restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
                @Override
                public void handleError(ClientHttpResponse response) throws IOException {
                    //当响应的值为400或401时候也要正常响应，不要抛出异常
                    if (response.getRawStatusCode() != 400 && response.getRawStatusCode() != 401) {
                        super.handleError(response);
                    }
                }
            });

            //发送请求,返回响应
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url,
                    HttpMethod.POST, new HttpEntity<MultiValueMap<String, String>>(bodyMap, headerMap),
                    Map.class);
            Map<String,String> responsebody = responseEntity.getBody();
            System.out.println(responsebody);

            if(responsebody == null || responsebody.get("access_token") == null || responsebody.get("refresh_token") == null || responsebody.get("jti") == null) {
                //jti是jwt令牌的唯一标识作为用户身份令牌
                throw new RuntimeException("创建令牌失败！");
            }

            authToken.setAccessToken(responsebody.get("access_token"));
            authToken.setRefreshToken(responsebody.get("refresh_token"));
            authToken.setJti(responsebody.get("jti"));
        }
        return authToken;
    }

    /***
     * base64编码
     * @param clientId
     * @param clientSecret
     * @return
     */
    private String httpbasic(String clientId,String clientSecret){
        //将客户端id和客户端密码拼接，按“客户端id:客户端密码”
        String string = clientId+":"+clientSecret;
        //进行base64编码
        byte[] encode = Base64Utils.encode(string.getBytes());
        return "Basic "+new String(encode);
    }
}
