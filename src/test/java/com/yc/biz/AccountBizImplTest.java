package com.yc.biz;

import com.yc.AppMain;
import com.yc.bean.Account;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {AppMain.class} )
@Slf4j
public class AccountBizImplTest {

    @Autowired
    private AccountBiz accountBiz;

    @Test
    public void findAccount() {
        Account a=accountBiz.findAccount(2);
        Assert.assertNotNull(a);
        log.info(a.toString());
    }

    @Test
    public void openAccount() {
        Account a=accountBiz.openAccount(126);
        Assert.assertNotNull(a);
        log.info(a.toString());
    }

    @Test
    public void deposite() {
        Account a=accountBiz.deposite(2,56);
        Assert.assertNotNull(a);
        log.info(a.toString());
    }

    @Test
    public void withdraw() {
        Account a=accountBiz.withdraw(11,600);
        Assert.assertNotNull(a);
        log.info(a.toString());
    }

    @Test
    public void transfer() {
        Account a=accountBiz.transfer(3,30,12);
        Assert.assertNotNull(a);
        log.info(a.toString());
    }


}