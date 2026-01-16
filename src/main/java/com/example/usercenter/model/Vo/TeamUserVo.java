package com.example.usercenter.model.Vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class TeamUserVo implements Serializable {
    private static final long serialVersionUID = -8630690765591996921L;
    private Long id;

    /**
     * 队长ID
     */
    private Long userId;

    /**
     * 队伍名称
     */
    private String teamName;

    /**
     * 招募描述
     */
    private String description;
    
    /**
     * 0 正常 1私密 2加密 
     */
    private Integer status;

    /**
     * 招募截止时间
     */
    private Date expireTime;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 标签列表
     */
    private String tags;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 关联的用户
     */
    private List<UserVo> userList;
    /**
     * 队长
     */
    private UserVo CreateUser;
}
