package com.example.usercenter.common;

import lombok.Data;
import java.io.Serializable;
/**
 * 统一返回类
 */
@Data
public class BaseResponse <T> implements Serializable {
    private Integer code;
    private T data;
    private String message;
    private String description;

    public BaseResponse(Integer code, T data, String message, String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }
    public BaseResponse(Integer code, String message) {
        this.code = code;
        this.message = message;
        this.data = null;
        this.description = "";
    }
    public BaseResponse(Integer code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
        this.data = null;
    }
}
