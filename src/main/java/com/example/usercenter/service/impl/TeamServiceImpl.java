package com.example.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.usercenter.Enum.TeamStatusEnum;
import com.example.usercenter.common.ErrorCode;
import com.example.usercenter.exception.BussinessException;
import com.example.usercenter.mapper.TeamUserMapper;
import com.example.usercenter.mapper.UserMapper;
import com.example.usercenter.model.Dto.TeamQuery;
import com.example.usercenter.model.TeamUser;
import com.example.usercenter.model.User;
import com.example.usercenter.model.Vo.TeamUserVo;
import com.example.usercenter.model.Vo.UserVo;
import com.example.usercenter.service.TeamService;
import com.example.usercenter.mapper.TeamMapper;
import com.example.usercenter.service.TeamUserService;
import com.example.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import com.example.usercenter.model.Team;
import javax.annotation.Resource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    @Autowired
    @Resource
    private TeamUserService teamUserService;
    @Autowired
    private UserService userService;
    @Resource
    private TeamUserMapper teamUserMapper;
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

    @Override
    public List<TeamUserVo> listTeam(TeamQuery teamQuery, Boolean isAdmin){
        /**
         * - 如果未登录/未传递参数,前20条左右
         * - 不展示**招募时间超时**的队伍，不展示私密队伍
         * - 如果传递了相关参数
         *   - 用户态：
         *     - 可以传递搜索文本：同时匹配标题/描述
         *     - 可以传递队伍名:精准查询
         *     - 可以传递描述：精准查询
         *     - 可以搜索队长名
         *     - 可以搜索最大的人数
         *     - 只可以搜索状态为“公开“的小队
         *   - 管理员：
         *     - 可以查询所有的小队
         * - 关联查询已经加入的用户
         * - 查询担任队长的用户
         */
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        /**
         * 模糊搜索队名和描述
         */
        if(StringUtils.isNotBlank(teamQuery.getSearchText())){
            teamQueryWrapper.and(teamQueryWrapper1 ->  teamQueryWrapper1.like("team_name",teamQuery.getSearchText()).or().like("description",teamQuery.getSearchText()));
        }
        /**
         * 查询队名
         */
        if(StringUtils.isNotBlank(teamQuery.getTeamname())){
            teamQueryWrapper.like("team_name",teamQuery.getTeamname());
        }
        /**
         * 根据描述查询
         */
        if(StringUtils.isNotBlank(teamQuery.getDescription())){
            teamQueryWrapper.like("description", teamQuery.getDescription());
        }
        /**
         * 查询>=max_num的小队
         */
        if(teamQuery.getMaxNum()!=null && teamQuery.getMaxNum()>0){
            teamQueryWrapper.ge("max_num",teamQuery.getMaxNum());
        }

        /**
         * 查询自己创建的小队
         */
        if(teamQuery.getUserid()!=null){
            teamQueryWrapper.eq("user_id",teamQuery.getUserid());
        }
        /**
         * 普通用户只能查看公开小队
         * 管理员可以全部查询
         */
        Integer userStatus = teamQuery.getStatus();
        TeamStatusEnum teamStatusEnum=TeamStatusEnum.getEnumByValue(userStatus);
        if(!TeamStatusEnum.PUBLIC.equals(teamStatusEnum)&&!isAdmin)
        {
            throw new BussinessException(ErrorCode.NO_AUTH,"没有权限");
        }
        if(userStatus!=null){
             teamQueryWrapper.eq("status",userStatus);
        }
        Page<Team> pager=new Page<>(teamQuery.getPage(),teamQuery.getPageSize());
        List<Team> teamList = this.page(pager,teamQueryWrapper).getRecords();
         if (CollectionUtils.isEmpty(teamList)) {
                return new ArrayList<>();
        }
        List<TeamUserVo> teamUserVOList = new ArrayList<>();
        //关联查询创建人的用户信息
        for (Team team : teamList) {
            TeamUserVo teamUserVO = new TeamUserVo();
            BeanUtils.copyProperties(team, teamUserVO);
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            Long teamId = team.getId();
            if (teamId != null) {
//                teamQueryWrapper.
                /**
                 *
                 * Selet * from User where id in (Select userid from team_user where teamid=teamId)
                 *
                 */
                QueryWrapper<TeamUser> teamUserQueryWrapper = new QueryWrapper<>();
                teamUserQueryWrapper.eq("teamid",teamId);
                List<?> userIdList=teamUserMapper.SelctUserFromId(teamId);
                if(userIdList!=null&&userIdList.size()>0){
                    QueryWrapper<User> UserQueryWrapper = new QueryWrapper<>();
                    UserQueryWrapper.in("id",userIdList);
                    List<User> userList= userService.list(UserQueryWrapper);
                    List<UserVo> userVoList = new ArrayList<>();
                    for(User user : userList){
                        UserVo userVo = new UserVo();
                        BeanUtils.copyProperties(user,userVo);
                        userVoList.add(userVo);
                    }
                    teamUserVO.setUserList(userVoList);
                }
            }
            User user = userService.getById(userId);
            //脱敏用户信息t
            if (user!=null){
                UserVo userVO = new UserVo();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }

    @Override
    public Boolean updateTeam(Team team, User loginUser) {
        //todo:检查当前传入值与已经存入的值是否一致，如果一致就不用操作，可以降低数据库使用次数
        /**
         * - 检查当前用户是否是要修改队伍的队长
         * - 是否更新Status
         *   - 如果更新
         *     - 加密->其他状态，清空Password
         *     - 其他状态->加密，必须补充密码
         *     - 加密->加密，补充密码
         * - name:队伍名称
         * - description:队伍描述
         * - expireTime:招募过期时间
         * - 队伍人数>1 且<=20
         *   - 队伍标题<20
         *   - 描述<512
         *   - 考虑踢出队伍/转让队伍权限
         */
        //检查当前用户是否登录
        if(loginUser==null){
            throw new BussinessException(ErrorCode.NO_LOGIN);
        }
        Team currentTeam=this.getById(team.getId());
        if(currentTeam==null){
            throw new BussinessException(ErrorCode.PARAM_ERROR);
        }
        //检查当前用户是否是要修改队伍的队长
        Long userId = currentTeam.getUserId();
        if(!userId.equals(loginUser.getId())){
            throw new BussinessException(ErrorCode.NO_AUTH);
        }
        String teamName = team.getTeamName();
        if(StringUtils.isNotBlank(teamName)){
            if(teamName.length()>20){
                throw new BussinessException(ErrorCode.PARAM_ERROR);
            }
        }
        String description = team.getDescription();
        if(StringUtils.isNotBlank(description)){
            if(description.length()>50){
                throw new BussinessException(ErrorCode.PARAM_ERROR);
            }
        }
        if(team.getExpireTime()!= null&& new Date().after(team.getExpireTime())){
            throw new BussinessException(ErrorCode.PARAM_ERROR);
        }

        //原本是Public/PRIVATE -> SECRET
        //必须要有密码
        TeamStatusEnum currentStatus=TeamStatusEnum.getEnumByValue(currentTeam.getStatus());
        TeamStatusEnum newStatus = TeamStatusEnum.getEnumByValue(team.getStatus());
        if(currentStatus.equals(TeamStatusEnum.PUBLIC)||currentStatus.equals(TeamStatusEnum.PRIVATE)){
            if(newStatus.equals(TeamStatusEnum.SECRET)){
                if(StringUtils.isBlank(team.getPassword())){
                    //必须要提供密码
                    throw new BussinessException(ErrorCode.PARAM_ERROR);
                }
            }
        }
        //如果原本是秘密的
        if(currentStatus.equals(TeamStatusEnum.SECRET)&&!newStatus.equals(TeamStatusEnum.SECRET)){
            if(!StringUtils.isBlank(team.getPassword())){
                throw new BussinessException(ErrorCode.PARAM_ERROR);
            }
        }
        //新的最大数量要检验
        if(team.getMaxNum()!=null&&(team.getMaxNum()<0||team.getMaxNum()>20)){
            throw new BussinessException(ErrorCode.PARAM_ERROR);
        }
        // 更新的最大大小如果比当前队伍中的人数小，报错
        QueryWrapper<TeamUser> teamQueryWrapper = new QueryWrapper<>();
        teamQueryWrapper.eq("teamid",team.getId());
        long counts=teamUserService.count();
        if(team.getMaxNum()!=null&&counts>team.getMaxNum()){
            throw new BussinessException(ErrorCode.PARAM_ERROR);
        }
        return  this.updateById(team);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean removeTeamById(Long Id, User loginUser) {
        if(loginUser==null){
            throw new BussinessException(ErrorCode.NO_LOGIN);
        }
        Team currentTeam=this.getById(Id);
        if(currentTeam==null){
            throw new BussinessException(ErrorCode.PARAM_ERROR);
        }
        if(!currentTeam.getUserId().equals(loginUser.getId())){
            throw new BussinessException(ErrorCode.NO_AUTH);
        }
        Long teamId = currentTeam.getId();
        QueryWrapper<TeamUser> teamQueryWrapper = new QueryWrapper<>();
        teamQueryWrapper.eq("teamid",teamId);
        boolean result=teamUserService.remove(teamQueryWrapper);
        if(!result){
            throw new BussinessException(ErrorCode.SYSTEM_ERROR);
        }else{
            boolean result2=this.removeById(teamId);
            if(!result2){
                throw new BussinessException(ErrorCode.SYSTEM_ERROR);
            }
            return true;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean joinTeam(Team team, User loginUser) {
        //todo:一个用户最多加入5个队伍
        //todo:私密队伍的加入需要校验（暂时只允许加入公开/加密队伍）
        //todo:不能加入已经过期的队伍
        if(loginUser==null){
            throw new BussinessException(ErrorCode.NO_LOGIN);
        }
        Team currentTeam=this.getById(team.getId());
        //当前加入的队伍是否存在
        if(currentTeam==null){
            throw new BussinessException(ErrorCode.PARAM_ERROR);
        }
        //TODO：此处直接比较用户输入的密码与数据库中的入队密码比较，后续改为用hash值
        //加密状态要校验
        TeamStatusEnum currentStatus=TeamStatusEnum.getEnumByValue(currentTeam.getStatus());
        if(currentStatus.equals(TeamStatusEnum.SECRET)){
            //保证有密码，且可以通过校验
            if(StringUtils.isBlank(team.getPassword())){
                throw new BussinessException(ErrorCode.PARAM_ERROR);
            }
            if(!team.getPassword().equals(currentTeam.getPassword())){
                throw new BussinessException(ErrorCode.NO_AUTH);
            }
            //
        }
        QueryWrapper<TeamUser> teamUserQueryWrapper = new QueryWrapper<>();
        teamUserQueryWrapper.eq("teamid",team.getId());
        long nowCounts=teamUserService.count(teamUserQueryWrapper);
        //保证当前队伍人未满
        if(nowCounts+1>currentTeam.getMaxNum()){
            throw new BussinessException(ErrorCode.SYSTEM_ERROR,"当前加入到队伍已满");
        }
        //判断当前是否已经加入该小队
        teamUserQueryWrapper.clear();
        teamUserQueryWrapper.eq("teamid",team.getId());
        teamUserQueryWrapper.eq("userid",loginUser.getId());
        if(teamUserService.count(teamUserQueryWrapper)>0){
            throw new BussinessException(ErrorCode.SYSTEM_ERROR,"当前已经加入");
        }
        TeamUser newTeamUser = new TeamUser();
        newTeamUser.setTeamid(team.getId());
        newTeamUser.setUserid(loginUser.getId());
        teamUserService.save(newTeamUser);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean withDrawTeam(Team team, User loginUser) {
        /**
         *- 检查
         *   - 当前用户是否登录
         *   - 该队伍是否存在
         *   - 当前用户是否已经加入了该队伍
         *   - 当前用户是否是当前队伍的队长
         *     - 是的话需要将队长改为第二早加入的用户
         * - 联动操作
         *   - 删除 队伍-用户表 中该用户的信息
         *     - 如果是队长，同步迁移队伍队长信息
         *
         */
        if(team==null){
            throw new BussinessException(ErrorCode.PARAM_ERROR);
        }
        //当前用户是否登录
        if(loginUser==null){
            throw new BussinessException(ErrorCode.NO_LOGIN);
        }
        //该队伍是否存在
        Team currentTeam=this.getById(team.getId());
        if(currentTeam==null){
            throw new BussinessException(ErrorCode.PARAM_ERROR,"当前队伍不存在");
        }
        //当前用户是否已经加入了该队伍
        QueryWrapper<TeamUser> teamUserQueryWrapper = new QueryWrapper<>();
        teamUserQueryWrapper.eq("teamid",team.getId());
        teamUserQueryWrapper.eq("userid",loginUser.getId());
        long count =teamUserService.count(teamUserQueryWrapper);
        if(count<=0){
            throw new BussinessException(ErrorCode.NO_AUTH,"未加入当前队伍");
        }
        //判断当前用户是否是当前队伍的队长
        if(currentTeam.getUserId().equals(loginUser.getId())){
            teamUserQueryWrapper.clear();
            teamUserQueryWrapper.eq("teamid",team.getId());
            //如果当前用户是队长
            List<TeamUser> teamUserList= teamUserMapper.SelectUserListDesc(team.getId());
            if(teamUserList.size()<2){
                //当前没有第二个用户，直接删除（解散）队伍
                this.removeTeamById(team.getId(),loginUser);
            }
            //下一个队长
            long nextCaptalUser=teamUserList.get(1).getUserid();
            currentTeam.setUserId(nextCaptalUser);
            Boolean updateResult=this.updateById(currentTeam);
            if(!updateResult){
                throw new BussinessException(ErrorCode.SYSTEM_ERROR);
            }
        }
            teamUserQueryWrapper.clear();
            teamUserQueryWrapper.eq("teamid",team.getId());
            teamUserQueryWrapper.eq("userid",loginUser.getId());
            Boolean withDrawResult=teamUserService.remove(teamUserQueryWrapper);
            if(!withDrawResult){
                throw new BussinessException(ErrorCode.SYSTEM_ERROR);
            }
            return true;
    }
}




