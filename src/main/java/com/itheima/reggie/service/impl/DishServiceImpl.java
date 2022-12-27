package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.Exception.CustomException;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lijia
 * @create 2022-11-16 11:55
 */
@Service
@Transactional
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;


    @Override
    public void saveWithFlavor(DishDto dishDto) {
        //保存dish信息
        this.save(dishDto);

        //save之后，雪花算法已经生成dishId
        Long dishId = dishDto.getId();

        //保存dishflavor信息
        List<DishFlavor> flavors = dishDto.getFlavors();
//        for (DishFlavor flavor : flavors) {
//            flavor.setDishId(dishId);
//        }

        flavors = flavors.stream().map(
                (item) -> {
                    item.setDishId(dishId);
                    return item;
                }
        ).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public DishDto getWithFlavorById(Long dishId) {
        DishDto dishDto = new DishDto();
        //首先查询dish基本信息
        Dish dish = super.getById(dishId);
        BeanUtils.copyProperties(dish, dishDto);
        //其次查询该dishId所对应的flavor数组
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishId);
        List<DishFlavor> dishFlavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(dishFlavors);
        return dishDto;
    }

    @Override
    public void updateWithFlavor(DishDto dishDto) {
        //首先修改dish基本信息
        super.updateById(dishDto);
        //其次删除Flavor口味信息
        Long dishId = dishDto.getId();
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(DishFlavor::getDishId, dishId);
        dishFlavorService.remove(queryWrapper);
        //再次添加该dishId相关的Flavor信息
        List<DishFlavor> dishFlavors = dishDto.getFlavors().stream().map(item -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(dishFlavors);
    }

    @Override
    public void deleteDish(List<Long> ids) {
        //判断所有菜品的状态 select count(*) from dish where id in(ids) and status= 1
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(Dish::getId,ids);
        dishLambdaQueryWrapper.eq(Dish::getStatus,1);
        Integer count = baseMapper.selectCount(dishLambdaQueryWrapper);
        if (count>0){
            throw new CustomException("该菜品正在售卖中，无法删除,如要删除，请先将其下架");
        }
        super.removeByIds(ids);
    }
}
