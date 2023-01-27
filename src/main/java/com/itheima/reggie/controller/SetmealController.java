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
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.cache.RedisCacheManager;
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
@Api(tags = "套餐相关接口")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisCacheManager redisCacheManager;

    /**
     * 分页带条件查询套餐信息
     * @param page 页码
     * @param pageSize  页大小
     * @param name 套餐名称(模糊匹配)
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("套餐信息分页查询接口")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name="page",value = "页码数",required = true),
            @ApiImplicitParam(name="pageSize",value = "页码大小",required = true),
            @ApiImplicitParam(name = "name",value = "套餐名称",required = false)
    })
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
    @ApiOperation("套餐信息添加接口")
    @CacheEvict(value="setmealCache",allEntries = true)
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
    @ApiOperation("套餐起售状态修改接口")
    @CacheEvict(value="setmealCache",allEntries = true)
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "state",value = "套餐的状态",required = true),
            @ApiImplicitParam(name = "ids",value = "套餐多个id组成的集合",required = true)
    })
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
    @CacheEvict(value="setmealCache",allEntries = true)
    @ApiOperation(value = "套餐删除接口")
    @ApiImplicitParam(name = "ids",value = "套餐多个id组成的集合")
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
    @ApiOperation(value = "根据套餐id获取套餐信息接口")
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
    @ApiOperation(value = "套餐信息修改接口")
    @CacheEvict(value="setmealCache",allEntries = true)
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
    @ApiOperation(value = "根据分类id查询相关套餐信息")
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId" ,unless = "#result.data == null")
    public R<List<Setmeal>> list(Setmeal setmeal){
        //根据categoryId查询相关套餐
        Long categoryId = setmeal.getCategoryId();
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,categoryId);
        setmealLambdaQueryWrapper.eq(Setmeal::getStatus,1);
        List<Setmeal> setmealList = setmealService.list(setmealLambdaQueryWrapper);
        //未查到对应套餐信息则引用指向空
//        if (setmealList.size() == 0) {setmealList = null;}
        return R.success(setmealList);
    }








}
