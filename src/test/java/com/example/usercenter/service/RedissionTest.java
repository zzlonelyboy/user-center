package com.example.usercenter.service;

import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedissionTest {
    @Resource
    private RedissonClient redissonClient;
    @Test
    public void testRedission()
    {
        RMap<String,Object> rMap= redissonClient.getMap("testMap");
//        rMap.put("街霸","老桑");
//        rMap.put("拳皇","火舞");
        rMap.remove("街霸");
//        System.out.println(rMap.get("街霸"));
//        System.out.println(rMap.get("拳皇"));
        //删除整个存的testMap
        rMap.delete();
    }
    @Test
    void testWatchDog() {
        RLock lock = redissonClient.getLock("yupao:precachejob:docache:lock");
        try {
            // 只有一个线程能获取到锁
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                Thread.sleep(300000);//todo 实际要执行的代码
                System.out.println("getLock: " + Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }
}
