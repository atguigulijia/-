package com.itheima.reggie.util;
import com.itheima.reggie.Exception.CustomException;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
/**
 * @author lijia
 * @create 2022-10-27 9:30
 * 通过qq邮箱发送验证码
 */
public class EmailUtil {
    private static final String EMAIL_HOSTNAME = "smtp.qq.com";    //
    private static final String SEND_EMAIL_ACCOUNT = "1295905922@qq.com";   //发件人邮箱账户
    private static final String SEND_EMAIL_PWD = "dqliciisjonxhjeb";   //发件人邮箱密码
    private static final String EMAIL_TITILE = "瑞吉外卖";  //邮件主题
    private static char[] verifiCode = null;    //通用验证码

    public static char[] sendEmailRegCode(String accpectAccount)   {
        try {
            HtmlEmail email = new HtmlEmail();
            email.setHostName(EMAIL_HOSTNAME);
            email.setCharset("utf-8");
            email.setAuthentication(SEND_EMAIL_ACCOUNT, SEND_EMAIL_PWD);
            email.setFrom(SEND_EMAIL_ACCOUNT, "ikun");
            email.addTo(accpectAccount);
            email.setSubject(EMAIL_TITILE);
            //生成验证码
            verifiCode = RandomCode.generateCheckCode();
            email.setTextMsg("你好，欢迎使用瑞吉外卖，你正在申请注册账户，你的验证码为" + String.valueOf(verifiCode) + ",有效时间为3分钟");
            email.send();
            return verifiCode;
        } catch (EmailException e) {
           throw new CustomException("邮箱发送失败");
        }
    }



    public static void main(String[] args) {
            sendEmailRegCode("1295905922@qq.com");
    }
}
