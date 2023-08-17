package com.yc.biz;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yc.bean.Account;
import com.yc.bean.OpRecord;
import com.yc.bean.OpType;
import com.yc.mappers.AccountMapper;
import com.yc.mappers.OpRecordMapper;
import io.undertow.client.ClientStatistics;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
@Transactional
@Primary
public class AccountBizImpl implements AccountBiz {
//    @Autowired
//    private AccountDao accountDao;
//    @Autowired
//    private OpRecordDao opRecordDao;

    @Autowired
    private AccountMapper accountDao;
    @Autowired
    private OpRecordMapper opRecordDao;

    @Value("${spring.mail.username}")
    private String from;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private RedisTemplate redisTemplate;

    //查询总金额
    @Override
    public Double findTotalBalance(){
        QueryWrapper<Account> wrapper1 = new QueryWrapper<>();
        wrapper1.select("sum(balance) as total");
        List<Map<String, Object>> list = accountDao.selectMaps(wrapper1);
        if (list!=null && list.size()>0){
            Map<String, Object> map = list.get(0);
            if (map.containsKey("total")){
                return Double.parseDouble(map.get("total").toString());
            }
        }
        return 0.0;
    }

    @Transactional(readOnly = true)
    @Override
    public Account findAccount(int accountId) {
        return this.accountDao.selectById(accountId);
    }

    //发送邮件
    @Override
    public String sendSimpleMail(String to) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo("2711837871@qq.com");
        String subject = "银行金额报表";
        message.setSubject(subject);
        String context = "尊敬的行长，今日的银行总金额为:" + findTotalBalance() + ",请查看  肖子涵";
        message.setText(context);
        javaMailSender.send(message);
        return "邮件发送成功";
    }

    @Override
    public Account openAccount(double money) {
        //开户操作 返回新的账号的id
       // int accountid=this.accountDao.insert(money);
        Account newAccount=new Account();
        newAccount.setMoney(money);
        this.accountDao.insert(newAccount);

        //包装日志信息
        OpRecord opRecord=new OpRecord();
        opRecord.setAccountid(newAccount.getAccountid());  //取出新增的账号
        opRecord.setOpmoney(money);
        opRecord.setOpType(OpType.DEPOSITE);
        //this.opRecordDao.insertOpRecord(opRecord);
        this.opRecordDao.insert(opRecord);

        //返回新的账户信息
//        Account a=new Account();
//        a.setAccountid(accountid);
//        a.setMoney(money);
        return newAccount;
    }

    @Override
    public Account deposite(int accountId, double money) {
        return this.deposite(accountId,money,null);
    }

    @Override
    public Account deposite(int accountId, double money, Integer transferid) {
        Account a=null;
        try {
            //a = this.accountDao.findById(accountId);
            a=this.accountDao.selectById(accountId);
        }catch (RuntimeException re){
            log.error(re.getMessage());  //TODO:封装保存日志的操作
            throw new RuntimeException("查无此账户"+accountId+"无法完成此操作");
        }
        //存款时 金额累加
        a.setMoney(a.getMoney()+money);

        //this.accountDao.update(accountId,a.getMoney());
        this.accountDao.updateById(a);

        OpRecord opRecord=new OpRecord();
        opRecord.setAccountid(accountId);
        opRecord.setOpmoney(money);
        if (transferid!=null){
            opRecord.setOpType(OpType.TRANSFER);
            opRecord.setTransferid(transferid);
        }else {
            opRecord.setOpType(OpType.DEPOSITE);
        }
        //this.opRecordDao.insertOpRecord(opRecord);
        this.opRecordDao.insert(opRecord);
        return a;
    }

    @Override
    public Account withdraw(int accountId, double money) {
        return this.withdraw(accountId,money,null);
    }

    @Override
    public Account withdraw(int accountId, double money, Integer transferid) {
        Account a=null;
        try {
            //a = this.accountDao.findById(accountId);
            a=this.accountDao.selectById(accountId);
        }catch (RuntimeException re){
            log.error(re.getMessage());  //TODO:封装保存日志的操作
            throw new RuntimeException("查无此账户"+accountId+"无法完成此操作");
        }
        //存款时 金额累加
        a.setMoney(a.getMoney()-money);
        OpRecord opRecord=new OpRecord();
        opRecord.setAccountid(accountId);
        opRecord.setOpmoney(money);
        if (transferid!=null){
            opRecord.setOpType(OpType.TRANSFER);
            opRecord.setTransferid(transferid);
        }else {
            opRecord.setOpType(OpType.WITHDRAW);
        }

        //this.opRecordDao.insertOpRecord(opRecord);  //先插入日志
        this.opRecordDao.insert(opRecord);
        //this.accountDao.update(accountId,a.getMoney()); //再减金额
        this.accountDao.updateById(a);
        return a;

    }

    @Override
    public Account transfer(int accountId, double money, int toaccountId) {
        //从accountId转money到 toAccountId
        this.deposite(toaccountId,money,accountId); //收款方
        //accountid从账户中取money
        Account a=this.withdraw(accountId,money,toaccountId);
        return a;
    }

    /**
     * 查流水
     * @param date
     * @return
     */
    @Override
    public List<OpRecord> findOpRecord(String date) {
        List<OpRecord> list = new ArrayList<>();
        //验证date格式是否正确
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date d = null;
        try {
            d = df.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            log.error("带查询日期格式不正确，原格式："+ date + "，要求格式：yyyy-MM-dd");
            throw new RuntimeException(e);
        }

        //日期：List<OpRecord>  存到redis中的OpRecord是json字符串 =》 序列化 =》 OpRecord实现类  java.io.Serializable接口
        if (redisTemplate.hasKey(date)){
            list = redisTemplate.opsForList().range(date, 0, redisTemplate.opsForList().size(date));
            log.info("从缓存的键:"+ date +",取出的值为:"+ list);
            return list;
        }
        //再查询数据库， select * from opRecord where optime between startdate and enddate;
        QueryWrapper wrapper = new QueryWrapper();
        //计算后一天
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.DATE, 1);
        Date nextDate = cal.getTime();
        String nextDateString = df.format(nextDate);
        wrapper.between("optime", date, nextDateString);
        list = opRecordDao.selectList(wrapper);
        if (list!=null && list.size()>0){
            redisTemplate.delete(date);
            redisTemplate.opsForList().leftPush(date, list);
            redisTemplate.expire(date, 15, TimeUnit.DAYS);
        }

        return list;
    }


}

