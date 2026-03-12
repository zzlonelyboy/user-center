package com.example.usercenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.usercenter.model.TeamUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author zz
* @description 针对表【team_user(队伍表)】的数据库操作Mapper
* @createDate 2026-01-08 20:45:59
* @Entity generator.domain.TeamUser
*/
@Mapper
public interface TeamUserMapper extends BaseMapper<TeamUser> {
    public List<Long> SelctUserFromId(Long teamid);
    public List<TeamUser> SelectUserListDesc(Long teamid);
}




