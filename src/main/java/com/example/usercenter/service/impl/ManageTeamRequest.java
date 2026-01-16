package com.example.usercenter.service.impl;

import lombok.Data;

@Data
public class ManageTeamRequest {
    Long teamId;
    Long userId;
    //todo:考虑邀请加入
    Integer operate; //0 踢出队伍 1 添加用户
}
