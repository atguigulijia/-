package com.itheima.reggie.common;

import lombok.Data;

/**
 * @author lijia
 * @create 2022-11-15 22:19
 */
@Data
public class BaseContext {
    private static ThreadLocal  threadLocal = new ThreadLocal();

    public static void setContext(Long id){
        threadLocal.set(id);
    }

    public static Long getContext(){
       return (Long) threadLocal.get();
    }
}
