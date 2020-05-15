package com.web.service.impl;

import com.spring.annotation.Autowried;
import com.spring.annotation.Service;
import com.web.dao.UserDao;
import com.web.entity.User;
import com.web.service.UserService;

@Service
public class UserServiceImpl implements UserService {
    @Autowried
    private UserDao userDao;

    @Override
    public User getUser() {
        return userDao.getUser();
    }

    @Override
    public void insertUser(User user) {
        userDao.insertUser(user);
    }
}
