package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * @author lijia
 * @create 2022-12-04 21:53
 * 订单控制器
 */
@RestController
@RequestMapping("/order")
@Slf4j
public class OrdersController {
    @Autowired
    private OrdersService ordersService;

    /**
     * 分页待条件查询订单信息
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(@RequestParam Integer page,
                          @RequestParam Integer pageSize,
                          String number,
                          String beginTime,
                          String endTime){
        // greater equals >= less than
        //select * from orders where id = number and order_time >= beginTime and order_time <=endTime;
        Page<Orders> ordersPage = new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper = new LambdaQueryWrapper<>();
        ordersLambdaQueryWrapper.like(!StringUtils.isEmpty(number),Orders::getNumber,number);
        ordersLambdaQueryWrapper.ge(!StringUtils.isEmpty(beginTime),Orders::getOrderTime,beginTime);
        ordersLambdaQueryWrapper.le(!StringUtils.isEmpty(beginTime),Orders::getOrderTime,endTime);
        ordersLambdaQueryWrapper.orderByDesc(Orders::getOrderTime); //最近的订单
        ordersPage = ordersService.page(ordersPage, ordersLambdaQueryWrapper);
        return R.success(ordersPage);
    }

    /**
     * 提交订单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("用户提交订单");
        ordersService.submit(orders);
        return R.success("提交订单成功");
    }


}
