package com.offcn.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;

@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {

    private static final String URL = "http://localhost:9100/oauth/login";
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String[] urls = {"/api/user/login","/oauth/login"};

        //设置放行的url地址
        if(Arrays.asList(urls).contains(request.getURI().getPath())){
            return chain.filter(exchange);
        }

        //从请求头中获取token
        String token = request.getHeaders().getFirst("Authorization");
        if(StringUtils.isEmpty(token)){
            //从cookie中获取token信息
            HttpCookie cookie = request.getCookies().getFirst("Authorization");
            if(!ObjectUtils.isEmpty(cookie)){
                token = cookie.getValue();
            }
        }

        if(StringUtils.isEmpty(token)){
            String forwordUrl = request.getURI().toString();
            try {
                //对这个url进行编码
                forwordUrl = URLEncoder.encode(forwordUrl, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            //设置跳转的页面
            response.getHeaders().add("Location",URL+"?to="+forwordUrl);
            response.setStatusCode(HttpStatus.SEE_OTHER);
            return response.setComplete();
        }

        //将token信息添加到请求头中
        //request.getHeaders().add("Authorization","Bearer "+token);
        request.mutate().header("Authorization","Bearer "+token);

        //请求放行
        return chain.filter(exchange);
    }


    //当启动运行这个微服务的时候,就创建这个过滤器对象
    @Override
    public int getOrder() {
        return 0;
    }
}
