package com.example.usercenter.service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import com.example.usercenter.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class UserServiceTests {

    @Resource
    private UserService userService;
    @Test
    void test(){
        User user = new User();
        user.setUsername("dogyupi");
        user.setUserAccount("123");
        user.setAvatarUrl("https://p6-passport.byteacctimg.com/img/mosaic-legacy/3795/3044413937~100x100.awebp");
        user.setGender(0);
        user.setUserPassword("12345678");
        user.setPhone("123");
        user.setEmail("4444");
        user.setUserStatus(0);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setIsDelete(0);
        userService.save(user);
        System.out.println(user.getId());////mybatis-plus会自动将 mybatis generatekey设置位true并返回，具体参考黑马课程9 mybatis
        Assertions.assertNotNull(user.getId());
    }

    @Test
    void userRegister() {
        String userAccount = "yupi";
        String  userPassword="";
        String checkPassword="12345678";
        long result = userService.userRegister(userAccount,userPassword,checkPassword);
        //如果确定报错是-1可用Equals校验
        Assertions.assertEquals(-1,result);

        userAccount="yu";
        result = userService.userRegister(userAccount,userPassword,checkPassword);
        Assertions.assertEquals(-1,result);

        userAccount="yupi";
        userPassword="123456";
        result = userService.userRegister(userAccount,userPassword,checkPassword);
        Assertions.assertEquals(-1,result);

        userAccount="dogyupi";
        result = userService.userRegister(userAccount,userPassword,checkPassword);
        Assertions.assertEquals(-1,result);

        userAccount="yu pi";
        userPassword="12345678";
        checkPassword="12345678";
        result = userService.userRegister(userAccount,userPassword,checkPassword);
        Assertions.assertEquals(-1,result);

        userAccount="yupi";
        userPassword="12345678";
        checkPassword="123456789";
        result = userService.userRegister(userAccount,userPassword,checkPassword);
        Assertions.assertEquals(-1,result);

        userAccount="yupiy";
        userPassword="12345678";
        checkPassword="12345678";
        result = userService.userRegister(userAccount,userPassword,checkPassword);
        Assertions.assertTrue(result>0);
    }
    @Test
    void SerachUserByTagsTest(){
        List<String> taglist=new ArrayList<>();
        taglist.add("java");
        taglist.add("python");
        long start = System.currentTimeMillis();
        List<User> users=userService.SearchUsersByTag(taglist);
        long end = System.currentTimeMillis();
        System.out.println("No-SQL="+(end-start));
        System.out.println(users.size());
//        return end-start;
    }
    @Test
    void SerachUserByTagsSQLTest(){
        List<String> taglist=new ArrayList<>();
        taglist.add("java");
        taglist.add("python");
        long start = System.currentTimeMillis();
        List<User> users=userService.SearchUsersByTagSQL(taglist);
        long end = System.currentTimeMillis();
        System.out.println("SQL="+(end-start));
        System.out.println(users.size());
//        return end-start;
    }
    @Test
    void TestInterface(){
//        long sum1=0;
//        long sum2=0;
//        for(Integer i=0;i<10;i++){
        //先进行一次，测试一连接数据库，避免连接数据库时间的干扰
        SerachUserByTagsTest();


        SerachUserByTagsTest();
        SerachUserByTagsSQLTest();
//        }
//        System.out.println("‘sum1/10+","+sum2);
//        User user=new User();
//        user.setUsername("dogyupi");
    }
}
