package com.example.usercenter.service;

import com.example.usercenter.model.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author zz
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-11-05 21:49:04
*/
public interface UserService extends IService<User> {
//接口方法默认public
    Long userRegister(String userAccount, String userPassword, String checkPassword);

    User dologin(String userAccount, String password, HttpServletRequest request);

    User getSafetyUser(User user);

    boolean checkRole(HttpServletRequest request);
    void userLogout(HttpServletRequest request);
    List<User> recommend(Integer page, Integer pageSize,Integer userID);
    List<User> SearchUsersByTagSQL(List<String> taglist);

    List<User> SearchUsersByTag(List<String> taglist);

    Boolean updateUser(User user, User loginUser);
    User getCurrentUser(HttpServletRequest request);
    Boolean isAdmin(User user);

    List<User> matchUser(User user, Integer pageSize,Integer page);
}
