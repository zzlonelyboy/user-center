package com.example.usercenter.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.usercenter.model.User;
import com.example.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class preCacheJob {
    @Resource
    UserService userService;
    @Resource
    RedisTemplate redisTemplate;
    //缓存活跃用户
    @Resource
    RedissonClient redissonClient;
    private List<Long> mainUserList = Arrays.asList(4L);
    @Scheduled(cron = "25 27 21 * * ?")
    public void doCacheUser(){
        RLock lock = redissonClient.getLock("yupao:preCacheJob:lock");
        try{
            if(lock.tryLock(0,-1,TimeUnit.MILLISECONDS)){
                System.out.println("getlock:"+Thread.currentThread().getId());
                for(Long id:mainUserList){
                 String searchFormat=String.format("yupao:user:recommend:%s",id);
                 IPage<User> userPage= (IPage<User>) redisTemplate.opsForValue().get(searchFormat);
                    if(userPage==null){
                        QueryWrapper queryWrapper = new QueryWrapper<>();
        //                List<User> userList=userService.list(queryWrapper);
                        IPage<User> pager=new Page<>(1,20);
                        userPage=userService.page(pager,queryWrapper);
                        redisTemplate.opsForValue().set(searchFormat,userPage,30, TimeUnit.MINUTES);
                    }
                }
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }finally {
            if(lock.isHeldByCurrentThread()){
                System.out.println("unlock:"+Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }
}
