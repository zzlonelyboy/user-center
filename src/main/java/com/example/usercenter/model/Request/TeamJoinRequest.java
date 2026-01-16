package com.example.usercenter.model.Request;

import lombok.Data;

import java.io.Serializable;

@Data
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID = -1422358916686358507L;
    private long teamId;
    private String password;
}
