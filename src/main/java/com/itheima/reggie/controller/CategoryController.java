package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author lijia
 * @create 2022-11-16 9:59
 */
@RestController
@Slf4j
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;


    @GetMapping("/page")
    public R<Page<Category>> page(int page,int pageSize){
        Page<Category> pageInfo = new Page<>(page,pageSize);
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Category::getSort);
        Page<Category> categoryPage = categoryService.page(pageInfo, queryWrapper);
        return R.success(categoryPage);
    }

    @PostMapping
    public R<String> save(@RequestBody Category category){
        categoryService.save(category);
        return R.success("添加分类成功");
    }
    @DeleteMapping
    public R<String> delete(@RequestParam Long ids){
//        categoryService.removeById(ids);
        categoryService.deleteByids(ids);
        return R.success("类别删除成功");
    }

    @PutMapping
    public R<String> update(@RequestBody Category category){
        categoryService.updateById(category);
        return R.success("修改类别成功");
    }

    /**
     * 显示分类菜品或套餐信息
     * @param type
     * @return
     */
    @GetMapping("list")
    public R<List<Category>> list(Integer type){
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(type!=null,Category::getType,type);
        queryWrapper.orderByDesc(Category::getSort);
        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }
}
