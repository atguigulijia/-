package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author lijia
 * @create 2022-12-13 22:06
 * 购物车
 */
@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {


    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("添加购物车信息:"+shoppingCart.toString());
        //设置userId
        Long userId = BaseContext.getContext();
        shoppingCart.setUserId(userId);

        //设置查询的userId条件
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId,userId);
        //首先判断添加的是菜品还是套餐
        Long dishId = shoppingCart.getDishId();
        if (StringUtils.isEmpty(dishId)){
            //套餐
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }else {
            //菜品
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getDishId,dishId);
        }

        //根据用户id和套餐id或者菜品id进行查询,判断是否购物车中是否已经存在
        //select * from shoppingCart where user_id = ? and dish_id/setmeal_id= ?
        ShoppingCart cartServiceOne = shoppingCartService.getOne(shoppingCartLambdaQueryWrapper);

        if (cartServiceOne !=null){
            //存在该菜品或套餐 -》 在对应的菜品或套餐记录的number+1
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number+1);
            //执行更新语句sql操作
            shoppingCartService.updateById(cartServiceOne);
        }else {
            //不存在 -》添加该菜品或套餐到购物车
            shoppingCart.setNumber(1);
            shoppingCartService.save(shoppingCart);
            //为了方便返回统一对象
            cartServiceOne = shoppingCart;
        }

        return R.success(cartServiceOne);
    }

    /**
     * 查询当前用户的购物车
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("查询购物车");
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId,BaseContext.getContext());
        shoppingCartLambdaQueryWrapper.orderByDesc(ShoppingCart::getCreateTime); //最近添加的菜品在最上面
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(shoppingCartLambdaQueryWrapper);
        return R.success(shoppingCartList);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        //sql: delete from shopping_cart where user_id = ?
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId,BaseContext.getContext());
        shoppingCartService.remove(shoppingCartLambdaQueryWrapper);
        return R.success("清空购物车成功");
    }

    /**
     * 减少购物车中的菜品或者套餐数量
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){
        //设置当前userID
        Long userId = BaseContext.getContext();
        shoppingCart.setUserId(userId);
        //判断是菜品还是套餐
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId,userId);
        Long dishId = shoppingCart.getDishId();
        if (dishId!=null){
            //菜品
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else {
            //套餐
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        //查询并判断当前用户的购物车中的(该菜品或套餐)数量是否为1
        ShoppingCart cartServiceOne = shoppingCartService.getOne(shoppingCartLambdaQueryWrapper);

        if (cartServiceOne == null){
            return R.error("未查询到该菜品或套餐");
        }
        Integer number = cartServiceOne.getNumber();
        if (number == 1){
            //数量为1直接移除
            shoppingCartService.remove(shoppingCartLambdaQueryWrapper);
            cartServiceOne = null;
        }else {
            //数量减去1
            cartServiceOne.setNumber(cartServiceOne.getNumber() -1);
        }

        return R.success(cartServiceOne);
    }


}
