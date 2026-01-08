package com.example.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.usercenter.model.Team;
import com.example.usercenter.model.User;

/**
* @author zz
* @description 针对表【team(队伍表)】的数据库操作Service
* @createDate 2026-01-08 16:35:00
*/
public interface TeamService extends IService<Team> {
    public Long addTeam(Team team, User loginUser);
}
