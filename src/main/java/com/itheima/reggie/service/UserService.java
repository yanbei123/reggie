package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

public interface UserService extends IService<User> {

    //发送手机短信验证码
    public R<String> sendMsg(User user);

    //移动端用户登录
    public R<User> loginUser(Map map, HttpSession session);

    //用户退出
    public R<String> logOutUser(HttpServletRequest request);
}
