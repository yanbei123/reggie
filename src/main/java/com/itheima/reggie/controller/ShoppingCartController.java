package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 燕北
 * @Date: 2022/07/04/16:43
 * @Description: 购物车
 */
@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("购物车信息:{}",shoppingCart);

        //设置用户id，指定当前是哪个用户的购物车数据
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        Long dishId = shoppingCart.getDishId();
        //当前购物车用户
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);

        if (dishId != null){
            //添加的是菜品
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        }else {
            //添加的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        //查询当前菜品或套餐是否存在于购物车中
        ShoppingCart cartOne = shoppingCartService.getOne(queryWrapper);

        if (cartOne != null){
            //如果已存在，则数量加一
            Integer number = cartOne.getNumber();
            cartOne.setNumber(number + 1);
            shoppingCartService.updateById(cartOne);
        }else {
            //如果不存在，设置数量为1，添加购物车
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartOne = shoppingCart;
        }

        return R.success(cartOne);
    }

    /**
     * 展示购物车
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        //按照添加时间升序排序
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        /*LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);*/
        String msg = shoppingCartService.cleanShoppingCart();
        return R.success(msg);
    }

    /**
     * 购物车对商品进行数量减一操作
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){
        //获取当前用户
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);

        //获取dishId进行判断，操作的是菜品还是套餐
        Long dishId = shoppingCart.getDishId();

        if (dishId != null){
            //当前操作的是菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else {
            //当前操作的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        //查询当前菜品或套餐是否存在于购物车中
        ShoppingCart cartOne = shoppingCartService.getOne(queryWrapper);

        if (cartOne != null){
            //如果已存在，判断数量是否大于1
            Integer number = cartOne.getNumber();
            if (number > 1){
                //大于1则数量减一
                cartOne.setNumber(number - 1);
                shoppingCartService.updateById(cartOne);
            }else {
                //否则直接删除
                shoppingCartService.removeById(cartOne);
                cartOne = shoppingCart;
            }
        }else {
            //如果不存在，购物车异常
            throw new CustomException("购物车异常");
        }

        return R.success(cartOne);
    }

}


