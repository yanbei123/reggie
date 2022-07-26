package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 燕北
 * @Date: 2022/07/04/16:41
 * @Description: 购物车
 */
public interface ShoppingCartService extends IService<ShoppingCart> {

    //清空购物车
    public String cleanShoppingCart();
}
