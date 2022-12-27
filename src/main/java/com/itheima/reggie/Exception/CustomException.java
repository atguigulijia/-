package com.itheima.reggie.Exception;

/**
 * @author lijia
 * @create 2022-11-16 12:16
 */
public class CustomException extends RuntimeException{

    public CustomException(String errorMsg){
        super(errorMsg);
    }

    public CustomException(){
        super();
    }
}
