package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

import java.util.List;

/**
 * @author lijia
 * @create 2022-11-16 11:54
 */
public interface DishService extends IService<Dish> {
    void saveWithFlavor(DishDto dishDto);

    DishDto getWithFlavorById(Long dishId);

    void updateWithFlavor(DishDto dishDto);

    void deleteDish(List<Long> ids);
}
