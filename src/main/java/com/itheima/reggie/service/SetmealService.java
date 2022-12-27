package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

/**
 * @author lijia
 * @create 2022-11-16 11:55
 */
public interface SetmealService extends IService<Setmeal> {
    void saveWithDishs(SetmealDto setmealDto);

    void deleteWithDishs(List<Long> ids);

    SetmealDto getWithSetmealDishById(Long id);

    void updateWithSetmealDish(SetmealDto setmealDto);
}
