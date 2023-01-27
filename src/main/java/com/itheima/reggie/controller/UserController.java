package com.itheima.reggie.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.util.EmailUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author lijia
 * @create 2022-12-04 23:13
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;
    @PostMapping("/login")
    public R<User> login(@RequestBody Map<String, String> map, HttpSession session) {
        String phone = map.get("phone");
        String code = map.get("code");
//        String regVerifiCode = (String) session.getAttribute("regVerifiCode");
        //从redis中获取对应手机的验证码
        String regVerifiCode = (String) redisTemplate.opsForValue().get(phone);
        if (StringUtils.isEmpty(regVerifiCode)) return R.error("验证码已失效，请重新获取验证码");


        //进行验证码比对
        if (regVerifiCode.equalsIgnoreCase(code)){
            //判断是否为新用户，新用户直接注册并登录
            User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getPhone,phone));
            if (user == null){
                //新用户
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
//                session.removeAttribute("regVerifiCode");   //登录后验证码失效
                session.setAttribute("user",user.getId());
                //清除验证码
                redisTemplate.delete(phone);
                return R.success(user);
            }
            //老用户
            if (user.getStatus() ==0){
                return R.error("账号已禁用");
            }
//            session.removeAttribute("regVerifiCode"); //登录后验证码失效
            session.setAttribute("user",user.getId());
            //清除验证码
            redisTemplate.delete(phone);
            return R.success(user);
        }
        return R.error("验证码错误，登录失败");
    }

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        String phone = user.getPhone();
        if (!StringUtils.isEmpty(phone)){
                String regVerifiCode = String.valueOf(EmailUtil.sendLoginCodeByEmail(phone));
                log.info("验证码为"+regVerifiCode);
//                session.setAttribute("regVerifiCode",regVerifiCode);
//                session.setMaxInactiveInterval(3 *60);

                //将验证码缓存在redis中  key为手机号
                redisTemplate.opsForValue().set(phone,regVerifiCode,3, TimeUnit.MINUTES);
                return R.success("邮箱验证码发送成功");
        }
        return R.error("邮箱号码不能为空");
    }
}
