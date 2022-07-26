package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrderService;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 燕北
 * @Date: 2022/07/08/15:57
 * @Description: 订单
 */
@RestController
@Slf4j
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 提交订单信息
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("当前订单信息:{}",orders);
        /*orders.setUserId(BaseContext.getCurrentId());
        log.info("orders:{}",orders);*/
        orderService.submit(orders);
        return R.success("订单提交成功");
    }

    /**
     * 手机端分页订单信息
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page<OrdersDto>> page(int page,int pageSize){

        //构造分页器
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>(page,pageSize);

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //获取当前用户id的订单，避免获取其他人的订单信息
        queryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByDesc(Orders::getOrderTime);
        orderService.page(pageInfo,queryWrapper);

        //对象拷贝 orderDetail ==>  ordersDto
        BeanUtils.copyProperties(pageInfo,ordersDtoPage,"records");
        List<Orders> records = pageInfo.getRecords();

        List<OrdersDto> list = records.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);
            //获取用户订单id
            Long orderId = item.getId();

            List<OrderDetail> orderDetailList = orderDetailService.getOrderDetailListOrderById(orderId);
            ordersDto.setOrderDetails(orderDetailList);
            return ordersDto;
        }).collect(Collectors.toList());
        log.info("list:{}",list);

        ordersDtoPage.setRecords(list);
        return R.success(ordersDtoPage);
    }
   /* public R<Page<OrdersDto>> page(int page,int pageSize){

        //构造分页器
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>(page,pageSize);

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //获取当前用户id的订单，避免获取其他人的订单信息
        queryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByDesc(Orders::getOrderTime);
        orderService.page(pageInfo,queryWrapper);

        //对象拷贝 orderDetail ==>  ordersDto
        BeanUtils.copyProperties(pageInfo,ordersDtoPage,"records");
        List<Orders> records = pageInfo.getRecords();

        List<OrdersDto> list = records.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);
            //获取用户订单id
            Long userId = item.getUserId();

            Orders orders = orderService.getById(userId);
            if (orders != null){
                String userName = orders.getUserName();
                String phone = orders.getPhone();
                String address = orders.getAddress();
                String consignee = orders.getConsignee();
                ordersDto.setUserName(userName);
                ordersDto.setPhone(phone);
                ordersDto.setAddress(address);
                ordersDto.setConsignee(consignee);
            }
           *//* List<OrderDetail> orderDetailList = orderDetailService.getOrderDetailListOrderById(id);
            ordersDto.setOrderDetails(orderDetailList);*//*
            return ordersDto;
        }).collect(Collectors.toList());
        log.info("list:{}",list);

        ordersDtoPage.setRecords(list);
        return R.success(ordersDtoPage);
    }*/

    /**
     * 再来一单
     * @param map
     * @return
     */
    @PostMapping("/again")
    public R<String> again(@RequestBody Map<String,String> map){
        String id = map.get("id");
        long orderId = Long.parseLong(id);

        //条件构造器：查询订单信息
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId,orderId);
        List<OrderDetail> orderDetails = orderDetailService.list(queryWrapper);

        //清空购物车
        shoppingCartService.cleanShoppingCart();

        //用户id
        Long userId = BaseContext.getCurrentId();
        List<ShoppingCart> shoppingCartList = orderDetails.stream().map((item) -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setUserId(userId);
            //shoppingCart.setImage(item.getImage());
            BeanUtils.copyProperties(item, shoppingCart, "id", "orderId");
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());

        log.info("shoppingCartList:{}",shoppingCartList);

        shoppingCartService.saveBatch(shoppingCartList);

        return R.success("操作成功");
    }

    /**
     * 客户端分页订单明细
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page<Orders>> pageClient(int page, int pageSize, String number){
        log.info("page:{},pageSize:{}",page,pageSize);
        Page<Orders> pageClient = new Page<>(page,pageSize);

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(number),Orders::getNumber,number);
        //queryWrapper.in(Orders::getCheckoutTime,beginTime,endTime);
        queryWrapper.orderByDesc(Orders::getCheckoutTime);
        orderService.page(pageClient,queryWrapper);
        return R.success(pageClient);
    }

    /**
     * 派送 修改状态为status
     * @param map
     * @return
     */
    @PutMapping
    public R<String> delivery(@RequestBody Map<String,String> map){
        String id = map.get("id");
        long orderId = Long.parseLong(id);
        String statusStr = map.get("status");
        int status = Integer.parseInt(statusStr);

        if (id == null || statusStr == null){
            throw new CustomException("传入数据错误");
        }

        Orders orders = orderService.getById(orderId);
        orders.setStatus(status);
        orderService.updateById(orders);
        return R.success("订单状态修改成功");
    }
}
