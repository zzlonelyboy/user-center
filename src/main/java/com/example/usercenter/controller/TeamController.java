package com.example.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.usercenter.common.BaseResponse;
import com.example.usercenter.common.BaseResponseUtils;
import com.example.usercenter.common.ErrorCode;
import com.example.usercenter.exception.BussinessException;
import com.example.usercenter.model.Dto.TeamQuery;
import com.example.usercenter.model.Request.TeamAddRequest;
import com.example.usercenter.model.Request.TeamUpdateRequest;
import com.example.usercenter.model.Team;
import com.example.usercenter.model.User;
import com.example.usercenter.service.TeamService;
import com.example.usercenter.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/team")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class TeamController {
    @Resource
    private TeamService teamService;
    @Resource
    private UserService userService;
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest,HttpServletRequest request) {
        //https://developer.aliyun.com/article/1592220 存储时间差8h解决方案
        if(teamAddRequest==null){
            throw new BussinessException(ErrorCode.PARAM_ERROR);
        }
        Team team =new Team();
        User loginUser=userService.getCurrentUser(request);
//        team.setUserid(loginUser.getId());
//        team.setTeamname(teamAddRequest.getTeamName());
//        team.setDescription(teamAddRequest.getDescription());
//        team.setPassword(teamAddRequest.getPassword());
//        team.setStatus(teamAddRequest.getStatus());
//        team.setExpireTime(teamAddRequest.getExpireTime());
        BeanUtils.copyProperties(teamAddRequest,team);
        teamService.addTeam(team,loginUser);
        return BaseResponseUtils.success(team.getId());
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody long id) {
        if(id<=0){
            throw new BussinessException(ErrorCode.PARAM_ERROR);
        }
        boolean result=teamService.removeById(id);
        if(!result){
            throw new BussinessException(ErrorCode.SYSTEM_ERROR,"队伍删除失败");
        }
        return BaseResponseUtils.success();
    }
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTime(@RequestBody TeamUpdateRequest teamUpdateRequest) {
        if(teamUpdateRequest==null){
            throw new BussinessException(ErrorCode.PARAM_ERROR);
        }
        Team team =new Team();
        team.setTeamName(teamUpdateRequest.getTeamName());
        team.setDescription(teamUpdateRequest.getDescription());
        team.setPassword(teamUpdateRequest.getPassword());
        team.setStatus(teamUpdateRequest.getStatus());
        team.setExpireTime(teamUpdateRequest.getExpireTime());
        team.setTags(teamUpdateRequest.getTags());
        boolean result=teamService.updateById(team);
        if(!result){
            throw new BussinessException(ErrorCode.SYSTEM_ERROR,"更像失败");
        }
        return BaseResponseUtils.success();
    }
    @PostMapping("/list")
    public BaseResponse<IPage<Team>> search(@RequestBody TeamQuery teamQuery){
        if(teamQuery==null){
            throw new BussinessException(ErrorCode.PARAM_ERROR);
        }
        Team team =new Team();
        //可以省略用繁复的set操作
        BeanUtils.copyProperties(teamQuery,team);
        //按照team中所有的内容搜索
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        //Ipage是接口，page是实现，构建查询对象是用Page,但是对外返回用Ipage接口，避免依赖
        Page<Team> page=new Page<>(teamQuery.getPage(),teamQuery.getPageSize());
        IPage<Team> resultPage=teamService.page(page,queryWrapper);
        return BaseResponseUtils.success(resultPage);
    }
}
