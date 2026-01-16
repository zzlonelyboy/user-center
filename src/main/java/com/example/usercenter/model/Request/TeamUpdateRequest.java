package com.example.usercenter.model.Request;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class TeamUpdateRequest implements Serializable {
    private static final long serialVersionUID = -4129438192146741953L;
    private long id;
    private String teamName;
    private String description;
    private Date expireTime;
    private String tags;
    private Integer status;
    private String password;
}
