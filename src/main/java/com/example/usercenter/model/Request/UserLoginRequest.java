package com.example.usercenter.model.Request;


import lombok.Data;

import java.io.Serializable;
//避免序列化冲突
@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = 8404325945778632133L;
    private String userAccount;
    private String userPassword;
}
