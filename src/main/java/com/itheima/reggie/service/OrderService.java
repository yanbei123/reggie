package com.itheima.reggie.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Orders;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 燕北
 * @Date: 2022/07/08/15:53
 * @Description:
 */
public interface OrderService extends IService<Orders> {

    //用户下单
    public void submit(Orders orders);

    //客户端分页
    //public void pageClient(int page,int pageSize);
}
