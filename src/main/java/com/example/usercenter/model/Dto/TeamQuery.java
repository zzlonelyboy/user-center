package com.example.usercenter.model.Dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TeamQuery extends PageRequest{
    /**
     * id
     */
    private Long id;

    /**
     * id 列表
     */
    private List<Long> idList;

    /**
     * 搜索关键词（同时对队伍名称和描述搜索）
     */
    private String searchText;

    /**
     * 队伍名称
     */
    private String teamname;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 用户id
     */
    private Long userid;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;
}