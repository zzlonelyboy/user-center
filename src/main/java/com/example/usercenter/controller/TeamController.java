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
import com.example.usercenter.model.Request.TeamJoinRequest;
import com.example.usercenter.model.Request.TeamUpdateRequest;
import com.example.usercenter.model.Request.WithDrawRequest;
import com.example.usercenter.model.Team;
import com.example.usercenter.model.TeamUser;
import com.example.usercenter.model.User;
import com.example.usercenter.model.Vo.TeamUserVo;
import com.example.usercenter.model.Vo.UserVo;
import com.example.usercenter.service.TeamService;
import com.example.usercenter.service.TeamUserService;
import com.example.usercenter.service.UserService;
import com.example.usercenter.service.impl.ManageTeamRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/team")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class TeamController {
    @Resource
    private TeamService teamService;
    @Resource
    private UserService userService;
    @Autowired
    private TeamUserService teamUserService;

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
    public BaseResponse<Boolean> deleteTeam(Long id,HttpServletRequest request) {
        if(id<=0){
            throw new BussinessException(ErrorCode.PARAM_ERROR);
        }
        User loginUser=userService.getCurrentUser(request);
        boolean result=teamService.removeTeamById(id,loginUser);
        if(!result){
            throw new BussinessException(ErrorCode.SYSTEM_ERROR,"队伍删除失败");
        }
        return BaseResponseUtils.success();
    }
//    @PostMapping("/update")
//    public BaseResponse<Boolean> updateTime(@RequestBody TeamUpdateRequest teamUpdateRequest) {
//        if(teamUpdateRequest==null){
//            throw new BussinessException(ErrorCode.PARAM_ERROR);
//        }
//        Team team =new Team();
//        team.setTeamName(teamUpdateRequest.getTeamName());
//        team.setDescription(teamUpdateRequest.getDescription());
//        team.setPassword(teamUpdateRequest.getPassword());
//        team.setStatus(teamUpdateRequest.getStatus());
//        team.setExpireTime(teamUpdateRequest.getExpireTime());
//        team.setTags(teamUpdateRequest.getTags());
//        boolean result=teamService.updateById(team);
//        if(!result){
//            throw new BussinessException(ErrorCode.SYSTEM_ERROR,"更像失败");
//        }
//        return BaseResponseUtils.success();
//    }
    @PostMapping("/list")
    public BaseResponse<List<TeamUserVo>> search(@RequestBody TeamQuery teamQuery,HttpServletRequest request) {
        if(teamQuery==null){
            throw new BussinessException(ErrorCode.PARAM_ERROR);
        }
        Team team =new Team();
        //可以省略用繁复的set操作
        //BeanUtils.copyProperties(teamQuery,team);
        //按照team中所有的内容搜索
        //QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        //Ipage是接口，page是实现，构建查询对象是用Page,但是对外返回用Ipage接口，避免依赖
        //Page<Team> page=new Page<>(teamQuery.getPage(),teamQuery.getPageSize());
        //IPage<Team> resultPage=teamService.page(page,queryWrapper);
        Boolean isAdmin=userService.isAdmin(userService.getCurrentUser(request));
        List<TeamUserVo> userLis=teamService.listTeam(teamQuery,isAdmin);
        return BaseResponseUtils.success(userLis);
    }
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest,HttpServletRequest request) {
        if(teamUpdateRequest==null){
            throw new BussinessException(ErrorCode.PARAM_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamUpdateRequest,team);
        User loginUser=userService.getCurrentUser(request);
        Boolean isOK=teamService.updateTeam(team,loginUser);
        if(!isOK){
            throw new BussinessException(ErrorCode.SYSTEM_ERROR,"更新失败");
        }
        return BaseResponseUtils.success(isOK);
    }
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request){
        if(teamJoinRequest==null){
            throw new BussinessException(ErrorCode.PARAM_ERROR);
        }
        User user=userService.getCurrentUser(request);
        if(user==null){
            throw new BussinessException(ErrorCode.NO_LOGIN);
        }
        Team team=new Team();
        BeanUtils.copyProperties(teamJoinRequest,team);
        team.setId(teamJoinRequest.getTeamId());
        Boolean result=teamService.joinTeam(team,user);
        return BaseResponseUtils.success(result);
    }
    @PostMapping("/withDraw")
    public BaseResponse<Boolean> withDrawTeam(@RequestBody WithDrawRequest withDrawRequest, HttpServletRequest request){
        if(withDrawRequest==null){
            throw new BussinessException(ErrorCode.PARAM_ERROR);
        }
        User user=userService.getCurrentUser(request);
        if(user==null){
            throw new BussinessException(ErrorCode.NO_LOGIN);
        }
        Team team=new Team();
        team.setId(withDrawRequest.getTeamid());
        Boolean result=teamService.withDrawTeam(team,user);
        return BaseResponseUtils.success(result);
    }
    @PostMapping("/manageTeam")
    public BaseResponse<Boolean> ManageTeam(@RequestBody ManageTeamRequest manageTeamRequest, HttpServletRequest request){
        if(manageTeamRequest==null){
            throw new BussinessException(ErrorCode.PARAM_ERROR);
        }

        //保证已经登录
        User loginUser=userService.getCurrentUser(request);
        if(loginUser==null){
            throw new BussinessException(ErrorCode.NO_LOGIN);
        }
        Team team=teamService.getById(manageTeamRequest.getTeamId());
        //保证操作者是队长
        if(!loginUser.getId().equals(team.getUserId())){
            throw new BussinessException(ErrorCode.NO_AUTH);
        }
        //删除用户（可以复用withdraw的作用）
        Boolean result=false;
        if(manageTeamRequest.getOperate()==0){
            User user = userService.getById(manageTeamRequest.getUserId());
            if(user==null){
                throw new BussinessException(ErrorCode.NO_AUTH);
            }
            result=teamService.withDrawTeam(team,user);
        }
        //添加用户
        if(manageTeamRequest.getOperate()==1){
            User user = userService.getById(manageTeamRequest.getUserId());
            if(user==null){
                throw new BussinessException(ErrorCode.NO_AUTH);
            }
            try{
                result=teamService.joinTeam(team,user);
                if(!result){
                    throw new BussinessException(ErrorCode.PARAM_ERROR);
                }
            }catch (Exception e){
                throw new BussinessException(ErrorCode.SYSTEM_ERROR);
            }
//            result=true;
        }
        return BaseResponseUtils.success(result);
    }
    @PostMapping("/getCreatedTeamList")
    public BaseResponse<List<TeamUserVo>> getCreatedTeamList(HttpServletRequest request){
        User loginUser=userService.getCurrentUser(request);
        if(loginUser==null){
            throw new BussinessException(ErrorCode.NO_LOGIN);
        }
        TeamQuery teamQuery=new TeamQuery();
        teamQuery.setUserid(loginUser.getId());
        List<TeamUserVo>teamList=teamService.listTeam(teamQuery,true);
        return BaseResponseUtils.success(teamList);
    }
    @PostMapping("/JoinedTeam")
    public BaseResponse<List<TeamUserVo>> getjoinedTeam(HttpServletRequest request){
        User loginUser=userService.getCurrentUser(request);
        if(loginUser==null){
            throw new BussinessException(ErrorCode.NO_LOGIN);
        }
        QueryWrapper<TeamUser> teamUserQuery=new QueryWrapper<>();
        teamUserQuery.eq("userid",loginUser.getId());
        /**
         * 获取所有我加入的小队
         */
        List<TeamUser> joinedTeam=teamUserService.list(teamUserQuery);
        List<TeamUserVo> teamList=new ArrayList<>();
        for(TeamUser teamUser:joinedTeam){
            TeamQuery teamQuery=new TeamQuery();
            teamQuery.setId(teamUser.getTeamid());
//            teamQuery.setUserid(teamUser.getUserid());
//            BeanUtils.copyProperties(teamUser,teamQuery);
            //查询自己加入的队伍，那么默认认为是有权利查看私人和加密队伍的
            List<TeamUserVo> tempTeamList= teamService.listTeam(teamQuery,true);
            teamList.addAll(tempTeamList);
        }
        return BaseResponseUtils.success(teamList);
    }

}
