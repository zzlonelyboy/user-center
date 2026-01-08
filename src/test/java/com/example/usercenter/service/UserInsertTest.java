package com.example.usercenter.service;

import com.example.usercenter.model.User;
import com.example.usercenter.service.impl.UserServiceImpl;
import org.springframework.util.StopWatch;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest
public class UserInsertTest {
    @Resource
    private UserService userService;
    private ExecutorService executorService = new ThreadPoolExecutor(16, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));

    @Test
    public void insertUserInfo() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        int INSERT_NUM=100000;
        List<User> userList=new ArrayList<User>();
        for (int i =0;i<INSERT_NUM;i++) {
            User user = new User();
            user.setUsername("假沙鱼");
            user.setUserAccount("yusha");
            user.setAvatarUrl("shanghai.myqcloud.com/shayu931/shayu.png");
            user.setUserProfile("一条咸鱼");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("123456789108");
            user.setEmail("shayu-yusha@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setTags("[]");
            userList.add(user);
        }
        userService.saveBatch(userList,20);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

        @Test
    public void doConcurrencyInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100000;
        // 分十组
        int j = 0;
        //批量插入数据的大小
        int batchSize = 5000;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        // i 要根据数据量和插入批量来计算需要循环的次数。（鱼皮这里直接取了个值，会有问题,我这里随便写的）
        for (int i = 0; i < INSERT_NUM/batchSize; i++) {
            List<User> userList = new ArrayList<>();
            while (true){
                j++;
                User user = new User();
                user.setUsername("假沙鱼");
                user.setUserAccount("yusha");
                user.setAvatarUrl("shanghai.myqcloud.com/shayu931/shayu.png");
                user.setUserProfile("一条咸鱼");
                user.setGender(0);
                user.setUserPassword("12345678");
                user.setPhone("123456789108");
                user.setEmail("shayu-yusha@qq.com");
                user.setUserStatus(0);
                user.setUserRole(0);
//                user.setPlanetCode("931");
                user.setTags("[]");
                userList.add(user);
                if (j % batchSize == 0 ){
                    break;
                }
            }
            //异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
                System.out.println("ThreadName：" + Thread.currentThread().getName());
                userService.saveBatch(userList,batchSize);
            },executorService);
                futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();

        stopWatch.stop();
        System.out.println( stopWatch.getLastTaskTimeMillis());

    }
}

