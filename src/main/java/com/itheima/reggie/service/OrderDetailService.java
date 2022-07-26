package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.OrderDetail;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 燕北
 * @Date: 2022/07/08/15:55
 * @Description:
 */
public interface OrderDetailService extends IService<OrderDetail> {
    /**
     * 通过订单id查询订单明细，得到一个订单集合
     * @param orderId
     * @return
     */
    public List<OrderDetail> getOrderDetailListOrderById(Long orderId);
}
