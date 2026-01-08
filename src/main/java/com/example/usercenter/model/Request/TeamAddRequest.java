package com.example.usercenter.model.Request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

//import java.util.Date;
//
@Data
public class TeamAddRequest implements Serializable {
    private static final long serialVersionUID = 5566044688958064082L;
    private String teamName;
    private String description;
    private Integer maxNum;
    private String password;
    private Integer status;
    private Date expireTime;
}
