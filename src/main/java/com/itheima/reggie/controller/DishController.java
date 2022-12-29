package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author lijia
 * @create 2022-11-17 9:27
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 分页查询所有菜品信息
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        Page<Dish> dishPage = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.like(!StringUtils.isEmpty(name), Dish::getName, name);
        dishLambdaQueryWrapper.orderByDesc(Dish::getUpdateTime);
        dishPage = dishService.page(dishPage, dishLambdaQueryWrapper);
        BeanUtils.copyProperties(dishPage, dishDtoPage, "records");
        List<DishDto> records = dishPage.getRecords().stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            //遍历record集合中的dish对象，拿到每个categoryId，再通过categoryId查询到categoryName
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (!StringUtils.isEmpty(category)) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

        //主要就是填充dishDtoPage对象中records集合
        dishDtoPage.setRecords(records);
        return R.success(dishDtoPage);
    }

    /**
     * 根据id获取菜品与菜品对应口味的信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id) {
        DishDto dishDto = dishService.getWithFlavorById(id);
        return R.success(dishDto);
    }

    /**
     * 添加菜品信息(包括菜品的口味)
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info("添加菜品信息："+dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        //清除所有缓存数据
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        return R.success("菜品添加成功");
    }

    /**
     * 更新菜品(包括菜品的口味)信息
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);
        //清除所有缓存数据
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        return R.success("更新成功");
    }

    /**
     * 批量或者单个修改菜品dish的状态
     * @param state
     * @param ids
     * @return
     */
    @PostMapping("/status/{state}")
    public R<String> status(@PathVariable Integer state,@RequestParam List<Long> ids){
        log.info("需要修改到的状态为"+state+"，ids为"+ids);
        Integer status = state;
        List<Dish> dishs = ids.stream().map(item ->{
            Dish dish = new Dish();
            dish.setId(item);
            dish.setStatus(state);
            return dish;
        }).collect(Collectors.toList());
        dishService.updateBatchById(dishs);
        return R.success("修改菜品状态成功");
    }

    /**
     * 根据dishId删除单个或多个菜品信息
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("需要删除的菜品ids有"+ids);
        dishService.deleteDish(ids);
        return R.success("菜品删除成功");
    }

    /***
     * 根据categoryId或者dishName获取对应的菜品集合(包括该菜品对应的口味信息)
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        List<DishDto> dishDtoList = null;
        Long categoryId = dish.getCategoryId();
        //按照菜品进行分类缓存
        //设计key=  "dish"+"_"+categoryId+"_"+status
        String key = "dish_"+categoryId+"_"+dish.getStatus();
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        //如果存在该菜品的缓存，则直接返回缓存数据
        if (dishDtoList!=null){
            return R.success(dishDtoList);
        }
        //不存在缓存数据，则进行菜品查询

        //首先查询根据categoryId和status条件 查询出所有dish信息
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,categoryId);
        dishLambdaQueryWrapper.eq(Dish::getStatus,1);
        //添加排序顺序
        dishLambdaQueryWrapper.orderByDesc(Dish::getUpdateTime); //查询最早的更新时间的dish
        List<Dish> dishList = dishService.list(dishLambdaQueryWrapper);

        //处理dishList，填充每个dishDto对象的flavor信息,categoryName信息
        dishDtoList = dishList.stream().map(item ->{
            //拷贝dish对象的属性给dishDto对象
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            //获取dish对象的categoryId
            Category category = categoryService.getById(dish.getCategoryId());
            if (category!=null){
                //填充dishDto对象的categoryName属性
                dishDto.setCategoryName(category.getName());
            }
            //获取dishId查询其对应的口信息
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            List<DishFlavor> dishFlavors = dishFlavorService.list(dishFlavorLambdaQueryWrapper);
            //设置dishDto的flavor集合属性
            dishDto.setFlavors(dishFlavors);
            return dishDto;
            }).collect(Collectors.toList());
        //完成首次菜品查询，将数据缓存起来24小时
        redisTemplate.opsForValue().set(key,dishDtoList,24, TimeUnit.DAYS);
        return R.success(dishDtoList);
    }






}
