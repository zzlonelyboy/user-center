package com.example.usercenter.service;

import com.example.usercenter.model.Team;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@SpringBootTest
public class TeamAddTest {
    @Test
    public void testDateTrans() {
        Team team = new Team();
        team.setExpireTime(new Date());
        String str=team.getExpireTime().toString();
        Gson gson = new Gson();
        String teamJson=gson.toJson(team.getExpireTime());
        System.out.println(str);
        System.out.println(teamJson);
    }
    @Test
    public void testTeam(){
        SortedMap<Integer, Long> indexDistanceMap = new TreeMap<>();
        indexDistanceMap.put(2,new Long(2));
        indexDistanceMap.put(1,new Long(3));
        for(Map.Entry<Integer, Long> entry:indexDistanceMap.entrySet()){
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }
        System.out.println(indexDistanceMap);
    }
}

