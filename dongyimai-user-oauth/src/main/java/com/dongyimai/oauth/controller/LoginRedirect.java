package com.dongyimai.oauth.controller;

import io.netty.util.internal.StringUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

@Controller
@RequestMapping("/oauth")
public class LoginRedirect {

    @RequestMapping("/login")
    public String login(@RequestParam(required = false) String to, Model model){
        String forwordUrl = to;
        if(StringUtils.hasText(forwordUrl)) {
            //对这个url进行解码
            try {
                forwordUrl = URLDecoder.decode(forwordUrl, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            model.addAttribute("forwordUrl", forwordUrl);
        }
        return "login";
    }
}
