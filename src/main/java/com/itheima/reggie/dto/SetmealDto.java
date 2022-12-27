package com.itheima.reggie.dto;

import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import lombok.Data;

import java.util.List;

/**
 * @author lijia
 * @create 2022-12-02 22:16
 */
@Data
public class SetmealDto extends Setmeal {
    //该套餐所包含的菜品
    private List<SetmealDish> setmealDishes;
    //该套餐所属类别名
    private String categoryName;
}
