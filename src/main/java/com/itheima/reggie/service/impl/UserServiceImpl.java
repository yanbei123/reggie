package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.mapper.UserMapper;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j

public class UserServiceImpl extends ServiceImpl<UserMapper,User> implements UserService{

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    /**
     * 发送手机短信验证码
     * @param user
     * @return
     */
    @Override
    public R<String> sendMsg(User user) {
        //获取手机号
        String phone = user.getPhone();

        //手机号不为空
        if (StringUtils.isNotEmpty(phone)){
            //生成随机验证码
            //String code = ValidateCodeUtils.generateValidateCode4String(4);
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code = {}",code);

            //调用阿里云提供的短信服务API完成发送短信
            //SMSUtils.sendMessage("瑞吉外卖","",phone,code);

            //将生成的验证码保存到session里
            //session.setAttribute(phone,code);
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);

            return R.success("验证码发送成功");
        }
        return R.error("短信发送失败");
    }

    /**
     * 移动端用户登录
     * @param map
     * @param session
     * @return
     */
    @Override
    public R<User> loginUser(Map map, HttpSession session) {
        log.info(map.toString());
        //获取手机号
        String phone = map.get("phone").toString();

        //获取验证码
        String code = map.get("code").toString();

        //从session中获取验证码
        //Object codeInSession = session.getAttribute(phone);

        //从redis中获取验证码
        Object codeInSession = redisTemplate.opsForValue().get(phone);

        //比对验证码
        if (codeInSession != null && codeInSession.equals(code)){
            //比对成功，登录成功
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);

            User user = userService.getOne(queryWrapper);
            //判断是否为新用户，如果是则直接登录
            if (user == null){
                //对新用户进行注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            //保存用户信息
            session.setAttribute("user",user.getId());
            //登录成功后清楚redis中的缓存信息
            redisTemplate.delete(phone);
            return R.success(user);
        }
        return R.error("短信发送失败");
    }

    /**
     * 用户退出
     * @param request
     * @return
     */
    @Override
    public R<String> logOutUser(HttpServletRequest request) {
        request.getSession().removeAttribute("user");
        return R.success("退出成功");
    }
}
