package com.yc.config;

import com.yc.biz.AccountBiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务
public class QuartzConfig {
    @Autowired
    private AccountBiz accountBiz;

//    @Scheduled(cron = "0/2 * * * * ? ")
//    public void getTotal(){
//        Double total = accountBiz.findTotalBalance();
//        System.out.println("银行总余额为:" + total);
//    }

//    @Scheduled(cron = "0/10 * * * * ? ")
//    public void sendEmail(){
//        accountBiz.sendSimpleMail("2711837871@qq.com");
//        System.out.println("成功");
//    }


}
