package com.example.usercenter.exception;

import com.example.usercenter.common.BaseResponse;
import com.example.usercenter.common.BaseResponseUtils;
import com.example.usercenter.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BussinessException.class)
    public BaseResponse businessExceptionHandler(BussinessException e){
        log.error("BussinessException:"+e.getMessage(),e);
        return new BaseResponse<>(e.getCode(),e.getMessage(),e.getDescription());
    }
    public BaseResponse runTimeExceptionHandler(RuntimeException e){
        log.error("RuntimeException:"+e.getMessage(),e);
        return BaseResponseUtils.error(ErrorCode.SYSTEM_ERROR);
    }
}
