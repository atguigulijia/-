package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.Exception.CustomException;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrdersMapper;
import com.itheima.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author lijia
 * @create 2022-12-04 21:52
 */
@Service
@Transactional
@Slf4j
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Override
    public void submit(Orders orders) {
        //获取当前用户id
        Long userId = BaseContext.getContext();
        //查询该用户的个人信息，购物车信息，地址信息

        //根据address_id查询address信息
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());
        if (addressBook == null) {
            throw new CustomException("当前用户地址信息错误，无法下单");
        }

        //根据user_id查询购物车信息
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(queryWrapper);
        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            throw new CustomException("当前用户的购物车为空，无法下单");
        }


        //根据user_id查询用户信息
        User user = userService.getById(userId);


        //利用mybatis-plus工具类生成order_id
        Long orderId = IdWorker.getId();
        //总金额
        AtomicInteger amount = new AtomicInteger(0);//保证原子性

        //将购物车的数据变为订单明细信息
        List<OrderDetail> orderDetailList =  shoppingCartList.stream().map(item ->{
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setName(item.getName());    //菜品或套餐名
            orderDetail.setImage(item.getImage());
            orderDetail.setOrderId(orderId);
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setNumber(item.getNumber());
            orderDetail.setAmount(item.getAmount());    //菜品或者套餐单价
            //累加总金额
            amount.addAndGet(item.getAmount().multiply(BigDecimal.valueOf(item.getNumber())).intValue());   //总价+=单价*数量
            return orderDetail;
        }).collect(Collectors.toList());


        //填充orders对象
        orders.setId(orderId);
        orders.setNumber(String.valueOf(orderId)); //订单号
        orders.setStatus(2); //待派送
        orders.setUserId(userId);
        orders.setAddressBookId(addressBook.getId());
        orders.setOrderTime(LocalDateTime.now());  //下单时间
        orders.setCheckoutTime(LocalDateTime.now());//结账时间
        orders.setAmount(new BigDecimal(amount.get())); //总金额
        orders.setPhone(user.getPhone());
        orders.setAddress(addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName()
                + addressBook.getCityName() == null ? "" : addressBook.getCityName()
                + addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName()
                + addressBook.getDetail() == null ? "" : addressBook.getDetail());
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());  //收货人
        //添加一条数据到orders表中
        this.save(orders);

        //添加多条数据到order_detail表中
        orderDetailService.saveBatch(orderDetailList);
        //清空购物车信息
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        shoppingCartService.remove(queryWrapper);
    }
}
