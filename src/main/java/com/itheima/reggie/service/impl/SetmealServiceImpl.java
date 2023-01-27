package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.Exception.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.events.Event;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lijia
 * @create 2022-11-16 11:55
 */
@Service
@Transactional
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;
    @Override
    public void saveWithDishs(SetmealDto setmealDto) {
        //保存套餐基本信息
        super.save(setmealDto);
        //保存套餐所对应的一些菜品
        Long setmealId = setmealDto.getId();
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //为setmealDishes集合中的setmealId属性赋值
        setmealDishes = setmealDishes.stream().map(item -> {
            item.setSetmealId(setmealId);
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    public void deleteWithDishs(List<Long> ids) {
        if (ids.size() ==0 || ids == null){
            throw new CustomException("套餐id不可为空");
        }
        //首先判断这些套餐是不是有起售状态
        //select count(*) from setmeal where id in (1,2,3) and status = 1;
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.in(Setmeal::getId,ids);
        setmealLambdaQueryWrapper.eq(Setmeal::getStatus,1);
        Integer count = baseMapper.selectCount(setmealLambdaQueryWrapper);
        if (count>0){
            //这些ids中存在起售状态，不可删除
            //存在起售中的套餐则不能删除 -》 抛出业务异常
            throw new CustomException("该套餐正在售卖中，无法删除,如要删除，请先将其下架");
        }
        //批量删除setmeal表中的对应setmeal信息(根据ids)
        super.removeByIds(ids);
        //删除setmealDish表中的dish信息(根据setmealId)
        //delete from setmeal_dish where setmeal_id in (1,2,3);
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(setmealDishLambdaQueryWrapper);
    }

    @Override
    public SetmealDto getWithSetmealDishById(Long id) {
        //根据id获取setmeal信息和setmealDish信息
        //首先根据id查询setmeal表中信息，用setmeal对象封装
        //在通过setmealId查询setmealDish表中信息，用list封装
        SetmealDto setmealDto = new SetmealDto();

        Setmeal setmeal = super.getById(id);
        BeanUtils.copyProperties(setmeal,setmealDto);

        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> setmealDishes = setmealDishService.list(setmealDishLambdaQueryWrapper);

        setmealDto.setSetmealDishes(setmealDishes);
        return setmealDto;
    }

    @Override
    public void updateWithSetmealDish(SetmealDto setmealDto) {
        //修改setmeal表中的基本信息
        this.updateById(setmealDto);
        Long setmealId = setmealDto.getId();
        //删除该setmealId所对应的所有菜品信息
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId,setmealId);
        setmealDishService.remove(setmealDishLambdaQueryWrapper);
        //添加setmealDish表信息
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map(item ->{
            item.setSetmealId(setmealId);
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);
    }
}
