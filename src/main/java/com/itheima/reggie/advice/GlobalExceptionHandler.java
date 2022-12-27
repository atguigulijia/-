package com.itheima.reggie.advice;

import com.itheima.reggie.Exception.CustomException;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 */
@ControllerAdvice(annotations = {RestController.class,Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 异常处理方法
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error(ex.getMessage());
        //java.sql.SQLIntegrityConstraintViolationException: Duplicate entry 'zhangsan' for key 'idx_username'
       if (ex.getMessage().contains("Duplicate entry")){
           String[] split = ex.getMessage().split(" ");
           return R.error(split[2]+"已经存在");
       }

        return R.error("未知错误");
    }

    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex){
        log.error(ex.getMessage());
        //java.sql.SQLIntegrityConstraintViolationException: Duplicate entry 'zhangsan' for key 'idx_username'
        return R.error(ex.getMessage());
    }


}
