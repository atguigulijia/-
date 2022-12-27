package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Category;

/**
 * @author lijia
 * @create 2022-11-16 9:56
 */
public interface CategoryService extends IService<Category> {
    void deleteByids(Long ids);
}
