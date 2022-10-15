package com.dongyimai.oauth;

import org.junit.jupiter.api.Test;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;

public class ParseJwtTest {

    @Test
    public void testParseToken(){
        //令牌
        String token="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlcyI6IlJPTEVfVklQLFJPTEVfVVNFUiIsIm5hbWUiOiJ1aml1eWUiLCJpZCI6IjEifQ.LpF7UgZ6xO3s0RISgmk8gRC3xQ9wxNmLySMnZkZPDhe9xtAmfHL38tRJqsAdtfF1ElCct8FujSfnWqfWK-kWQzrm_Hz7PbUI0S_F5IvggZC2S3lFQLSnKUyV7qzqy4sU_L8XJOugjUVYjuJYRSG2SmlrMPCRLv2ynvRy8E9XMb7UUf5N9Pdkwhm1pjI7mUqtatX8bRmnINB9HRE-o24W_wzJAV4t3c0seYgieFDeRRTlY0fbt_4JV1DJ0UZf-koPO4sBOeN1fO0BaXlbD8z8-gSAcXNraO9rwWIoKAKNFlEdRpBajzIlryDhvCzBGzgOnlfNpCnV3SJgqgYn-q1gDw";

        //公钥
        String publicKey="-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoXWga+nG/KeB81EHVyW3bd7XI4cjCwHsiWvhWSGGOE0lx4/woqrVav7OXhzfO0+nt8GgC+d8yi7JuhGOvf5J3opueVCTwgz0ZK1dazjNSv4zsNEWky37PdzEYgLMhtZyrTmNNqZeuhdEJkNZ709LxOWjMQAOdJFJCKqfFt1Y9m1Wn9QxdUg0lqZzUSjYBDPFSGFMY4NfS2m6iowVYpN5gNuorvGZjwhgL8e/fPKKChjtHEeONuddqgUY6OtoP8CASZ0+DnOIX03IDresLOxC09uEDsXbEhXmX21sFFCragUZ8LsXckY2vXKuUVkIyBveNJ9x3tafldJNf1rgH4LoTwIDAQAB-----END PUBLIC KEY-----";

        //解码
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publicKey));

        //获取解码后内容
        String claims = jwt.getClaims();
        System.out.println(claims);
    }
}
