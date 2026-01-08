package com.example.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.usercenter.common.BaseResponse;
import com.example.usercenter.common.BaseResponseUtils;
import com.example.usercenter.common.ErrorCode;
import com.example.usercenter.exception.BussinessException;
import com.example.usercenter.model.Request.UserLoginRequest;
import com.example.usercenter.model.Request.UserRegisterRequest;
import com.example.usercenter.model.User;
import com.example.usercenter.service.UserService;
import com.fasterxml.jackson.databind.ser.Serializers;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.example.usercenter.constant.UserConstant.USER_LOGIN_STATE;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if(userRegisterRequest ==null){
            throw new BussinessException(ErrorCode.NULL_ERROR,"请求为空");
//            return BaseResponseUtils.error(ErrorCode.NULL_ERROR);
        }
        //进行简单校验，Controller层对参数本身校验，不涉及业务逻辑
        String userAccount=userRegisterRequest.getUserAccount();
        String userPassword=userRegisterRequest.getUserPassword();
        String checkPassword=userRegisterRequest.getCheckPassword();
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword)){
               throw new BussinessException(ErrorCode.PARAM_ERROR,"账户/密码为空");
//            return BaseResponseUtils.error(ErrorCode.NULL_ERROR);
        }
        Long result=userService.userRegister(userAccount, userPassword, checkPassword);
        return BaseResponseUtils.success(result);
    }
    @PostMapping("/login")
    public BaseResponse<User> login(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if(userLoginRequest ==null){
            throw new BussinessException(ErrorCode.NULL_ERROR,"请求为空");
//            return BaseResponseUtils.error(ErrorCode.NULL_ERROR);
        }
        //进行简单校验，Controller层对参数本身校验，不涉及业务逻辑
        String userAccount=userLoginRequest.getUserAccount();
        String userPassword=userLoginRequest.getUserPassword();
        if(StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BussinessException(ErrorCode.NULL_ERROR,"密码/账户为空");
//            return BaseResponseUtils.error(ErrorCode.NULL_ERROR);
        }
        User user=userService.dologin(userAccount,userPassword,request);

        return BaseResponseUtils.success(user);
    }
    @GetMapping("/search")
    public BaseResponse<List<User>> search(String username,HttpServletRequest request) {
        //权限鉴定
        if(!userService.checkRole(request)){
            throw new BussinessException(ErrorCode.NO_AUTH);
//            return BaseResponseUtils.error(ErrorCode.NO_AUTH);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(username)){
//
            queryWrapper.like("username",username);
        }
        List<User>  list = userService.list(queryWrapper);
        List<User> userlist= list.stream().map(user->userService.getSafetyUser(user)).collect(Collectors.toList());
        return BaseResponseUtils.success(userlist);
    }
    @GetMapping("/search/tag")
    public BaseResponse<List<User>> searchTag(@RequestParam List<String> tagNameList,HttpServletRequest request) {
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BussinessException(ErrorCode.PARAM_ERROR);
        }
        List<User> list = userService.SearchUsersByTag(tagNameList);
        return BaseResponseUtils.success(list);
    }
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if(!userService.checkRole(request)){
            throw new BussinessException(ErrorCode.NO_AUTH);
//            return BaseResponseUtils.error(ErrorCode.NULL_ERROR);
        }
        if(id<0){
            return null;
        }
        //配置后会自动改为逻辑删除
        Boolean res=userService.removeById(id);
        return BaseResponseUtils.success(res);
    }
    @GetMapping("/current")
    public BaseResponse<User> currentUser(HttpServletRequest request) {
        Object userObject= request.getSession().getAttribute(USER_LOGIN_STATE);
        if(userObject==null){
            return BaseResponseUtils.error(ErrorCode.NO_LOGIN);
        }
        User user=(User)userObject;
        //todo 应该采用验证，而不是直接重新查询
        User current=userService.getById(user.getId());
        if(current==null){
            throw new BussinessException(ErrorCode.NO_LOGIN);
//            return BaseResponseUtils.error(ErrorCode.NO_LOGIN);
        }
        User safeUser=userService.getSafetyUser(current);
        return BaseResponseUtils.success(safeUser);
    }
    @PostMapping("/logout")
    public BaseResponse logout(HttpServletRequest request) {
        userService.userLogout(request);
        return BaseResponseUtils.success();
    }
    @PostMapping("/update")
    public BaseResponse updateUser(@RequestBody User user,HttpServletRequest request) {
        if(user==null){
            throw new BussinessException(ErrorCode.PARAM_ERROR);
        }
        User loginUser=this.userService.getCurrentUser(request);
        Boolean result=userService.updateUser(user,loginUser);
        return BaseResponseUtils.success(result);
    }
    @GetMapping("/recommendUser")
    //todo:此处的缓存对于仅对于用户而言存在，但是对于页数和分页不存在，此处的缓存需要与之适配，否则会出现每一页都返回第一页的情况
    //todo:是该完整查询，缓存完整表，还是缓存前几十页
    //todo:此处缓存还缓存了密码等不必要数据
    //TODO:转向业务逻辑层
    public BaseResponse recommendUser(@RequestParam Integer page,Integer pageSize, HttpServletRequest request){
        Object userObject= request.getSession().getAttribute(USER_LOGIN_STATE);
        if(userObject==null){
            throw new BussinessException(ErrorCode.NO_LOGIN);
        }
        User loginUser=(User)userObject;
        Long userID=loginUser.getId();
        String searchFormat=String.format("yupao:user:recommend:%s",userID);
        IPage<User> userPage= (IPage<User>) redisTemplate.opsForValue().get(searchFormat);
        if(userPage==null){
            QueryWrapper queryWrapper = new QueryWrapper<>();
    //        List<User> userList=userService.list(queryWrapper);
            IPage<User> pager=new Page<>(page,pageSize);
            userPage=userService.page(pager,queryWrapper);
            redisTemplate.opsForValue().set(searchFormat,userPage,30, TimeUnit.MINUTES);
        }
        List<User> userList=userPage.getRecords();
        userList.stream().map(user->userService.getSafetyUser(user)).collect(Collectors.toList());
        return BaseResponseUtils.success(userList);
    }
}
