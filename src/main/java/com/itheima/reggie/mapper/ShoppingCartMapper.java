package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 燕北
 * @Date: 2022/07/04/16:40
 * @Description: 购物车
 */
@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {
}
