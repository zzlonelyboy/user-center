package com.example.usercenter.model.Request;

import lombok.Data;

import java.io.Serializable;
@Data
public class WithDrawRequest implements Serializable {
    private static final long serialVersionUID = -2055448073302322975L;
    private  long teamid;
}
