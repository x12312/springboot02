package com.yc.controller;

import com.yc.bean.Account;
import com.yc.bean.OpRecord;
import com.yc.biz.AccountBiz;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Api(tags = "银行接口")
public class BankController {
    @Autowired
    private AccountBiz accountBiz;

    @GetMapping("/findOpRecord")
    @ApiOperation(value = "查询指定日期的日志")
    public Map findOpRecord(String date){
        Map result = new HashMap();
        try {
            List<OpRecord> list = this.accountBiz.findOpRecord(date);
            result.put("code", 1);
            result.put("data", list);
        }catch (RuntimeException ex){
            result.put("code", 0);
            result.put("errMag", ex.getMessage());
            ex.printStackTrace();
        }
        return result;
    }

    @PostMapping("/openAccount")
    @ApiOperation(value = "开户操作")
    public Account openAccount(double money){
        return accountBiz.openAccount(money);
    }

    @PostMapping("/deposite")
    @ApiOperation(value = "存款操作")
    public Account deposite(int accountid,double money ){
        return accountBiz.deposite(accountid,money);
    }

    @PostMapping("/withdraw")
    @ApiOperation(value = "取款操作")
    public Account withdraw(int accountid,double money){
        return accountBiz.withdraw(accountid,money);
    }

    @PostMapping("/transfer")
    @ApiOperation(value = "转账操作")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accountId", value = "转出账户", required = true),
            @ApiImplicitParam(name = "money", value = "操作金额", required = true),
            @ApiImplicitParam(name = "toAccountId", value = "转入账户", required = true),
    })
    public Account transfer(int accountid,double money,int toAccountId){
        return accountBiz.transfer(accountid,money,toAccountId);
    }

    @PostMapping("/findAccount")
    @ApiOperation(value = "查看操作")
    public Account findAccount(int accountId){
        return accountBiz.findAccount(accountId);
    }

}
