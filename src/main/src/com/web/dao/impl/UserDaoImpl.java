package com.web.dao.impl;

import com.spring.annotation.Service;
import com.web.dao.UserDao;
import com.web.entity.User;

@Service
public class UserDaoImpl implements UserDao {

    @Override
    public void insertUser(User user) {
        System.out.println("--->insert user:" + user.toString());
    }

    @Override
    public User getUser() {
        return new User("1", "Eric.He", 28);
    }
}
