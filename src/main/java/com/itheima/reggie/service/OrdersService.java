package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Orders;
import org.springframework.stereotype.Service;

/**
 * @author lijia
 * @create 2022-12-04 21:52
 */
public interface OrdersService extends IService<Orders> {
    void submit(Orders orders);
}
