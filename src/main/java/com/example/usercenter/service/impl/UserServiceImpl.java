package com.example.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.usercenter.common.ErrorCode;
import com.example.usercenter.exception.BussinessException;
import com.example.usercenter.model.User;
import com.example.usercenter.service.UserService;
import com.example.usercenter.mapper.UserMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.example.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.example.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author zz
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-11-05 21:49:04
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{
    private final String SALTCHAR = "yupi";
    //不用引入mapper bean 因为此处已经封装完毕
//    private final UserService userService;
//
//    public UserServiceImpl(UserService userService) {
//        this.userService = userService;
//    }
    /**
     * 如果传输的不是一个很多的数据的话，不用直接传对象
     *
     * @param userAccount 账户
     * @param userPassword 密码
     * @param checkPassword 校验密码
     * @return id
     */
    @Override
    public Long userRegister(String userAccount, String userPassword, String checkPassword) {
        //1.校验
        //原生很麻烦
//        if(userAccount==null ||userPassword==null||checkPassword==null||userAccount.length()<=0||){
//
//        }
        // 可以用一个高效库 apache commons Lang 中的StringUtils判空
        //注意isBlank与isAnyBlank的区别
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword)){
            //待优化
            throw new BussinessException(ErrorCode.PARAM_ERROR,"密码/账户为空");
        }
        if(userAccount.length()<4){
//            return (long) -1;
            throw new BussinessException(ErrorCode.PARAM_ERROR,"账户长度过短");
        }
        if(userPassword.length()<8||checkPassword.length()<8){
            throw new BussinessException(ErrorCode.PARAM_ERROR,"密码长度过短");
        }



//        //账户不能重复
//        //此时mybatisPlus生成的已经生成了常见的数据库增删改查
//        //查询数据库的操作，应该在所有的字符检验之后，避免资源占用
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("user_account",userAccount);
//        long count=this.count(queryWrapper);
//        if(count>0){
//            return -1;
//        }


        //账户不能包含特殊字符
        //正则使用
        String validPattern="\\pP|\\pS|\\s+";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()){
            throw new BussinessException(ErrorCode.PARAM_ERROR,"账户字符异常");
        }
        //账户不能重复
        if(!checkPassword.equals(userPassword)){
            throw new BussinessException(ErrorCode.PARAM_ERROR,"账户重复");
        }


        //账户不能重复
        //此时mybatisPlus生成的已经生成了常见的数据库增删改查
        //查询数据库的操作，应该在所有的字符检验之后，避免资源占用
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account",userAccount);
        long count=this.count(queryWrapper);
        if(count>0){
            throw new BussinessException(ErrorCode.PARAM_ERROR,"账户重复");
        }

        //加密
        String hashedPassword = DigestUtils.md5DigestAsHex((SALTCHAR+userPassword).getBytes());
        //插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(hashedPassword);


        boolean saveResult = this.save(user);
        if(!saveResult){
            throw new BussinessException(ErrorCode.NULL_ERROR,"写入失败");
        }
        //返回用户id
        //判断是否保存失败
        return user.getId();
    }

    /**
     *
     * @param userAccount  用户账户
     * @param userPassword 密码
     * @param request 请求
     * @return
     */
    @Override
    public User dologin(String userAccount, String userPassword, HttpServletRequest request) {
         if(StringUtils.isAnyBlank(userAccount,userPassword)){
            //todo 修改为自定义异常
//            return null;
             throw new BussinessException(ErrorCode.PARAM_ERROR,"密码/账户为空");
        }
        if(userAccount.length()<4){
//            return null;
            throw new BussinessException(ErrorCode.PARAM_ERROR,"账户过短");
        }
        if(userPassword.length()<8){
//            return null;
            throw new BussinessException(ErrorCode.PARAM_ERROR,"密码过短");
        }

        //账户不能包含特殊字符
        //正则使用
        String validPattern="\\pP|\\pS|\\s+";

        String hashedLoginPassword=DigestUtils.md5DigestAsHex((SALTCHAR+userPassword).getBytes());

        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()){
            throw new BussinessException(ErrorCode.PARAM_ERROR,"账户字符异常");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account",userAccount);
        queryWrapper.eq("user_password",hashedLoginPassword);
        User loginResult=this.getOne(queryWrapper);
        if(loginResult==null){
            log.info("user login fail,userAccount:{},userPassword:{}",userAccount,userPassword);
            throw new BussinessException(ErrorCode.NULL_ERROR,"用户/密码错误");
        }
        //脱敏操作
        User cleanUser=this.getSafetyUser(loginResult);
        //记录用户登录态
        request.getSession().setAttribute(USER_LOGIN_STATE,cleanUser);
        return cleanUser;

    }

    @Override
    public User getSafetyUser(User user){
        User cleanUser=new User();
        cleanUser.setId(user.getId());
        cleanUser.setUsername(user.getUsername());
        cleanUser.setUserAccount(user.getUserAccount());
        cleanUser.setAvatarUrl(user.getAvatarUrl());
        cleanUser.setGender(user.getGender() );
        cleanUser.setPhone(user.getPhone());
        cleanUser.setEmail(user.getEmail());
        cleanUser.setUserRole(user.getUserRole());
        cleanUser.setUserStatus(user.getUserStatus());
        cleanUser.setCreateTime(user.getCreateTime());
        cleanUser.setUserProfile(user.getUserProfile());
        cleanUser.setTags(user.getTags());
        return cleanUser;
    }
    @Override
    public boolean checkRole(HttpServletRequest request){
        Object userObject= request.getSession().getAttribute(USER_LOGIN_STATE);
        User user=(User)userObject;
        //判空，并判断是否是管理员,不是则返回空列表
        return userObject != null && user.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public void userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return;
    }
    @Override
    @Deprecated
    public List<User> SearchUsersByTagSQL(List<String> taglist){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        for (String tag:taglist){
            queryWrapper=queryWrapper.like("tags",tag);
        }
        return this.list(queryWrapper).stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    @Override
    public List<User> SearchUsersByTag(List<String> taglist) {

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userlist=this.list(queryWrapper);
        Gson gson = new Gson();
        return userlist.stream().filter(user->{
            String tagStr=user.getTags();
//            if(StringUtils.isBlank(tagStr)){
//                return false;
//            }
//            Set<String> tagSet = gson.fromJson(tagStr,new TypeToken<Set<String>>(){}.getType());
            //等价为
            Set<String> tagSet = gson.fromJson(tagStr,new TypeToken<Set<String>>(){}.getType());
            tagSet= Optional.ofNullable(tagSet).orElse(new HashSet<>());
            for(String tag :taglist){
                if(!tagSet.contains(tag)){
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    @Override
    public Boolean updateUser(User user, User loginUser) {
        if(user.getId().longValue()!=loginUser.getId().longValue()&&!this.isAdmin(loginUser)){
            throw new BussinessException(ErrorCode.NO_AUTH);
        }
        //如果是自己修改自己不用校验，但是如果是管理员修改，需要保证修改的用户是存在的
        User oldUser=this.getById(user.getId());
        if(oldUser==null){
            throw new BussinessException(ErrorCode.NULL_ERROR);
        }
        return this.updateById(user);
    }

    @Override
    public User getCurrentUser(HttpServletRequest request) {
        User loginUser=(User)request.getSession().getAttribute(USER_LOGIN_STATE);
        if(loginUser==null){
            throw new BussinessException(ErrorCode.NO_LOGIN);
        }
        return loginUser;
    }
    @Override
    public Boolean isAdmin(User user){
        if(user==null){
            throw new BussinessException(ErrorCode.NULL_ERROR);
        }
        if(user.getUserRole()!=ADMIN_ROLE){
            return false;
        }
        return true;
    }
}




