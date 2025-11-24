package com.example.usercenter.model.Request;

import lombok.Data;

import java.io.Serializable;
//避免序列化冲突
@Data
public class UserRegisterRequest implements Serializable {
    private static final long serialVersionUID = -6350040352654349777L;

    private String userAccount;
    private String userPassword;
    private String checkPassword;
}
