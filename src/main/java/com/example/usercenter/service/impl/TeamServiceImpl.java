package com.example.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.usercenter.model.Team;
import com.example.usercenter.service.TeamService;
import com.example.usercenter.mapper.TeamMapper;
import org.springframework.stereotype.Service;

/**
* @author zz
* @description 针对表【team(队伍表)】的数据库操作Service实现
* @createDate 2026-01-08 16:35:00
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

}




