package com.example.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.usercenter.common.ErrorCode;
import com.example.usercenter.exception.BussinessException;
import com.example.usercenter.model.TeamUser;
import com.example.usercenter.model.User;
import com.example.usercenter.service.TeamService;
import com.example.usercenter.mapper.TeamMapper;
import com.example.usercenter.service.TeamUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import com.example.usercenter.model.Team;
import javax.annotation.Resource;
import java.util.Date;

/**
* @author zz
* @description 针对表【team(队伍表)】的数据库操作Service实现
* @createDate 2026-01-08 16:35:00
*/
@Service
@Slf4j
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    // 定义常量，消除硬编码
    private static final int MAX_CREATE_TEAM_NUM = 5; // 最多创建5个队伍
    private static final int MIN_TEAM_MEMBER = 1;     // 队伍最少人数
    private static final int MAX_TEAM_MEMBER = 20;    // 队伍最多人数
    private static final int MAX_TEAM_NAME_LENGTH = 20; // 队名最大长度
    private static final int MAX_DESCRIPTION_LENGTH = 512; // 描述最大长度
    private static final int TEAM_STATUS_SECRET = 2; // 加密状态

    private final TransactionTemplate transactionTemplate;
    @Resource
    private TeamUserService teamUserService;

    // 构造器注入（推荐，替代@Resource，符合Spring规范）
    public TeamServiceImpl(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public Long addTeam(Team team, User loginUser) {
        // ========== 第一步：非事务操作 - 参数前置校验（提前失败，释放资源） ==========
        // 1. 请求参数非空校验
        if (team == null) {
            throw new BussinessException(ErrorCode.NULL_ERROR, "队伍信息不能为空");
        }
        // 2. 登录状态校验
        if (loginUser == null) {
            throw new BussinessException(ErrorCode.NO_LOGIN, "请先登录");
        }
        Long userId = loginUser.getId();

        // 3. 校验：用户最多创建5个队伍（修复逻辑+注释）
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        teamQueryWrapper.eq("user_id", userId);
        long createdTeamCount = this.count(teamQueryWrapper);
        if (createdTeamCount >= MAX_CREATE_TEAM_NUM) { // 改为>=，避免创建第6个
            throw new BussinessException(ErrorCode.NO_AUTH, "当前创建队伍数量已达上限（最多5个）");
        }

        // 4. 队伍人数校验
        Integer maxNum = team.getMaxNum();
        if (maxNum == null || maxNum < MIN_TEAM_MEMBER || maxNum > MAX_TEAM_MEMBER) {
            throw new BussinessException(ErrorCode.PARAM_ERROR,
                    String.format("队伍人数需在%d-%d之间", MIN_TEAM_MEMBER, MAX_TEAM_MEMBER));
        }

        // 5. 队名校验（非空+长度）
        String teamName = team.getTeamName();
        if (StringUtils.isBlank(teamName)) {
            throw new BussinessException(ErrorCode.PARAM_ERROR, "队名不能为空");
        }
        if (teamName.length() > MAX_TEAM_NAME_LENGTH) { // 修复：>20才报错（20个字符合法）
            throw new BussinessException(ErrorCode.PARAM_ERROR,
                    String.format("队名长度不能超过%d个字符", MAX_TEAM_NAME_LENGTH));
        }

        // 6. 描述校验（允许为空，但长度限制）
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new BussinessException(ErrorCode.PARAM_ERROR,
                    String.format("队伍描述长度不能超过%d个字符", MAX_DESCRIPTION_LENGTH));
        }

        // 7. 队伍状态校验（默认0，仅允许0/1/2）
        Integer currentStatus = team.getStatus();
        if (currentStatus == null) {
            currentStatus = 0; // 默认为公开状态
            team.setStatus(currentStatus);
        } else if (currentStatus < 0 || currentStatus > 2) {
            throw new BussinessException(ErrorCode.PARAM_ERROR, "队伍状态只能是0（公开）、1（私有）、2（加密）");
        }

        // 8. 加密状态必须有密码，且密码非空
        if (currentStatus == TEAM_STATUS_SECRET && StringUtils.isBlank(team.getPassword())) {
            throw new BussinessException(ErrorCode.PARAM_ERROR, "加密状态的队伍必须设置密码");
        }

        // 9. 超时时间校验（允许为空？若为空需处理，此处假设必填）
        Date expireTime = team.getExpireTime();
        if (expireTime == null) {
            throw new BussinessException(ErrorCode.PARAM_ERROR, "队伍过期时间不能为空");
        }
        if (new Date().after(expireTime)) {
            throw new BussinessException(ErrorCode.PARAM_ERROR, "队伍过期时间必须晚于当前时间");
        }

        // ========== 第二步：事务操作 - 仅包含数据库写操作 ==========
        return this.transactionTemplate.execute(new TransactionCallback<Long>() {
            @Override
            public Long doInTransaction(TransactionStatus transactionStatus) {
                try {
                    // 1. 插入队伍表（设置创建人ID）
                    team.setUserId(userId);
                    boolean saveTeamSuccess = save(team);
                    if (!saveTeamSuccess) { // 校验保存结果
                        throw new RuntimeException("队伍信息保存失败");
                    }

                    // 2. 插入用户-队伍关系表
                    TeamUser teamUser = new TeamUser();
                    teamUser.setTeamid(team.getId());
                    teamUser.setUserid(userId);
                    boolean saveTeamUserSuccess = teamUserService.save(teamUser);
                    if (!saveTeamUserSuccess) { // 校验保存结果
                        throw new RuntimeException("用户-队伍关系保存失败");
                    }

                    return team.getId();
                } catch (Exception e) {
                    // 捕获数据库操作异常，标记事务回滚
                    transactionStatus.setRollbackOnly();
                    // 封装异常信息，便于排查
                    log.error(e.getMessage(), e);
                    throw new BussinessException(ErrorCode.SYSTEM_ERROR,"创建队伍失败");
                }
            }
        });
    }
}




