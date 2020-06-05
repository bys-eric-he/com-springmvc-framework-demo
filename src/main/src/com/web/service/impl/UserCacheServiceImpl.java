package com.web.service.impl;

import com.alibaba.fastjson.JSON;
import com.spring.annotation.Service;
import com.web.entity.User;
import com.web.service.UserService;

import java.util.HashMap;

@Service("userCacheService")
public class UserCacheServiceImpl implements UserService {

    private HashMap<String, Object> objectHashMap = new HashMap<>();

    @Override
    public void insertUser(User user) {

        if (user != null) {
            synchronized (UserCacheServiceImpl.class) {
                objectHashMap.put(user.getId(), user);
                System.out.println(JSON.toJSONString(user));
            }
        }
    }

    @Override
    public User getUser(String id) {
        if (id != null) {
            return (User) objectHashMap.get(id);
        }
        return null;
    }
}
