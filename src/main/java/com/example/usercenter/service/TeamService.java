package com.example.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.usercenter.model.Dto.TeamQuery;
import com.example.usercenter.model.Team;
import com.example.usercenter.model.User;
import com.example.usercenter.model.Vo.TeamUserVo;
import com.example.usercenter.model.Vo.UserVo;

import java.util.List;

/**
* @author zz
* @description 针对表【team(队伍表)】的数据库操作Service
* @createDate 2026-01-08 16:35:00
*/
public interface TeamService extends IService<Team> {
    public Long addTeam(Team team, User loginUser);
    public List<TeamUserVo> listTeam(TeamQuery team, Boolean isAdmin);
    public Boolean updateTeam(Team team, User loginUser);
    public Boolean removeTeamById(Long Id, User loginUser);
    public Boolean joinTeam(Team team, User loginUser);
    public Boolean withDrawTeam(Team team, User loginUser);
//    public Boolean isCaptain(Team team, User loginUser);
}
