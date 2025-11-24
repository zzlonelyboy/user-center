package com.example.usercenter.common;

/**
 * 同意返回工具类，用于生成success和error消息封装
 */
public class BaseResponseUtils {
    public static <T> BaseResponse<T> success(T data){
        return new BaseResponse<>(0,data,"success","");
    }
    public static <T> BaseResponse<T> success(){
        return new BaseResponse<>(0,"success");
    }
    public static <T> BaseResponse<T> error(ErrorCode errorCode){
        return new BaseResponse<>(errorCode.getCode(),errorCode.getMessage(),errorCode.getDescription());
    }
}
