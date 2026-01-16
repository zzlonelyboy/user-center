package com.example.usercenter.model.Vo;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户信息脱敏后返回
 */
@Data
public class UserVo implements Serializable {

    private static final long serialVersionUID = 5961137931339297297L;

    private Long id;
    /**
     * 用户名
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 0 正常 1私密 2加密
     */
    private Integer userStatus;

    /**
     *
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;

    /**
     *
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 用户角色 0 普通用户 1管理员
     */
    private Integer userRole;

    private String userProfile;

    private String tags;
}
