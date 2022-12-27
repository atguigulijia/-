package com.itheima.reggie.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lijia
 * @create 2022-11-12 22:49
 */
@Configuration
@MapperScan("com.itheima.reggie.mapper")
public class MyPageConfig {

    //配置分页插件
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        // 你的最大单页限制数量，默认 500 条，小于 0 如 -1 不受限制
        paginationInterceptor.setLimit(100);
        return paginationInterceptor;
    }
}
