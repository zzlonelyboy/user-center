package com.example.usercenter.common;

public enum ErrorCode {
    SUCCESS(0,"Success",""),
    PARAM_ERROR(40000,"请求参数错误",""),
    NULL_ERROR(40001,"请求参数为空",""),
    NO_LOGIN(40002,"当前未登录",""),
    NO_AUTH(40003,"无权限访问",""),
    SYSTEM_ERROR(50000,"系统错误","");
    private int code;
    private String message;
    private String description;
    private ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }
}
