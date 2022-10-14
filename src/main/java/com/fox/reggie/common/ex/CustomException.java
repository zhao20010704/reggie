package com.fox.reggie.common.ex;

public class CustomException extends RuntimeException{
    /**
     * 删除失败异常
     * @param message
     */
    public CustomException(String message){
        super(message);
    }
}