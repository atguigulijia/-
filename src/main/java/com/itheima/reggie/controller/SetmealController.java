package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author lijia
 * @create 2022-12-01 22:23
 * 套餐控制器
 */
@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 分页带条件查询套餐信息
     * @param page 页码
     * @param pageSize  页大小
     * @param name 套餐名称(模糊匹配)
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(@RequestParam Integer page,@RequestParam Integer pageSize,String name){
        Page<Setmeal> setmealPage = new Page<>(page,pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.like(!StringUtils.isEmpty(name),Setmeal::getName,name);
        setmealLambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealPage = setmealService.page(setmealPage, setmealLambdaQueryWrapper);
        //拷贝属性
        BeanUtils.copyProperties(setmealPage,setmealDtoPage,"records");

        //处理records属性,为setmealDto对象的categoryName属性赋值
        List<SetmealDto> setmealDtos =  setmealPage.getRecords().stream().map(setmeal ->{
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(setmeal,setmealDto);
            //根据每个setmeal对象的categoryId进行category表查询categoryName
            Long categoryId = setmeal.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (!StringUtils.isEmpty(category)){
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(setmealDtos);
        return R.success(setmealDtoPage);
    }

    /**
     * 添加套餐信息包括该套餐下的菜品
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info(setmealDto.toString());
        setmealService.saveWithDishs(setmealDto);
        return R.success("添加套餐信息成功");
    }

    /**
     * 修改套餐的status
     * @param state
     * @param ids
     * @return
     */
    @PostMapping("/status/{state}")
    public R<String> status(@PathVariable Integer state,@RequestParam List<Long> ids){
        log.info("需要修改到的状态为:"+state+"ids有"+ids);
        List<Setmeal> setmeals = ids.stream().map(item ->{
            Setmeal setmeal = new Setmeal();
            setmeal.setId(item);
            setmeal.setStatus(state);
            return setmeal;
        }).collect(Collectors.toList());
        setmealService.updateBatchById(setmeals);
        return R.success(state==1?"启用":"禁用"+"套餐成功");
    }

    /**
     * 删除套餐信息
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        setmealService.deleteWithDishs(ids);
        return  R.success("删除套餐信息成功");
    }

    /**
     * 根据id获取套餐信息(包括套餐所对应的菜品)
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id){
        SetmealDto setmealDto = setmealService.getWithSetmealDishById(id);
        return R.success(setmealDto);
    }

    /**
     * 修改套餐信息（包括修改该套惨下所关联的菜品信息）
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.updateWithSetmealDish(setmealDto);
        return R.success("修改套餐信息成功");
    }

    /**
     * 根据categoryId查询相关套餐信息
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        //根据categoryId查询相关套餐
        Long categoryId = setmeal.getCategoryId();
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,categoryId);
        setmealLambdaQueryWrapper.eq(Setmeal::getStatus,1);
        List<Setmeal> setmealList = setmealService.list(setmealLambdaQueryWrapper);
        return R.success(setmealList);
    }








}
