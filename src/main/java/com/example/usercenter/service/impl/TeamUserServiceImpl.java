package com.example.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.usercenter.model.TeamUser;
import com.example.usercenter.mapper.TeamUserMapper;
import com.example.usercenter.service.TeamUserService;
import org.springframework.stereotype.Service;

/**
* @author zz
* @description 针对表【team_user(队伍表)】的数据库操作Service实现
* @createDate 2026-01-08 20:41:39
*/
@Service
public class TeamUserServiceImpl extends ServiceImpl<TeamUserMapper, TeamUser>
    implements TeamUserService {

}




