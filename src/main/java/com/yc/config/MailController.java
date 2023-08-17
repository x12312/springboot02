package com.yc.config;


import com.yc.biz.AccountBiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MailController {
    @Autowired
    private AccountBiz accountBiz;

    @RequestMapping("/send01")
    public String sendSimpleMail(){
        String to = "3371460041@qq.com";
        return accountBiz.sendSimpleMail(to);
    }
}
