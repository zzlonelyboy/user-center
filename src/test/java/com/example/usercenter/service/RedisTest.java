package com.example.usercenter.service;

import com.example.usercenter.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
@SpringBootTest

public class RedisTest {
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Test
    public void test(){
        ValueOperations valueOperations= redisTemplate.opsForValue();
        valueOperations.set("WuShuangjiang","fourteen");
        valueOperations.set("WuShuangjiang1",12);
        User user = new User();
        user.setId(1L);
        user.setUsername("shayu");
        valueOperations.set("shayuUser", user);

        Object WuShuangjiang = valueOperations.get("WuShuangjiang");
        Assertions.assertTrue("fourteen".equals((String) WuShuangjiang));
        WuShuangjiang = valueOperations.get("WuShuangjiang1");
        Assertions.assertTrue(12 == (Integer) WuShuangjiang);
        System.out.println(valueOperations.get("shayuUser"));
    }
}
