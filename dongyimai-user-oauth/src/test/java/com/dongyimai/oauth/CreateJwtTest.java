package com.dongyimai.oauth;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.rsa.crypto.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

public class CreateJwtTest {

    //测试令牌
    @Test
    public void testCreateToken(){
        //证书路径
        String key_location="dongyimai.jks";
        //秘钥库密码
        String key_password="dongyimai";
        //私钥密码
        String keypwd="dongyimai";
        //秘钥别名
        String alias="dongyimai";
        //访问证书路径
        ClassPathResource resource = new ClassPathResource(key_location);
        //创建秘钥工厂
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource, key_password.toCharArray());
        //读取秘钥对
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias, keypwd.toCharArray());
        //获取私钥
       RSAPrivateKey rsaPrivateKey= (RSAPrivateKey) keyPair.getPrivate();

        System.out.println("私钥:"+rsaPrivateKey);

       //定义数据
        Map<String,Object> map=new HashMap<>();
        map.put("id","1");
        map.put("name","ujiuye");
        map.put("roles","ROLE_VIP,ROLE_USER");

        //生成jwt令牌
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(map), new RsaSigner(rsaPrivateKey));
        //取出令牌
        String token = jwt.getEncoded();
        System.out.println("token:"+token);
    }


    @Test
    public  void test1() {
        String password="123";
        System.out.println(new BCryptPasswordEncoder().encode(password));
    }
}
