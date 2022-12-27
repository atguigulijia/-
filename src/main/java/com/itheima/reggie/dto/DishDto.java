package com.itheima.reggie.dto;

import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lijia
 * @create 2022-11-21 11:04
 * dto: data transfer object 数据传输对象
 */
@Data
public class DishDto extends Dish {
    //菜品对应的口味信息
    private List<DishFlavor> flavors = new ArrayList<>();
    //分类名称
    private String categoryName;
    private Integer copies;

}
