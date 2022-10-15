package com.offcn.test;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class AppTest {

    @Test
    public void method(){
       /* String pwd = "123";

        String encode = new BCryptPasswordEncoder().encode(pwd);
        System.out.println(encode);*/
        for (int i = 0; i < 5; i++) {
            double random = Math.random();
            int a = (int) (random*1000000);
            System.out.println(a);
        }
    }
}
