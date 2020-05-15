package com.web.dao;

import com.web.entity.User;

public interface UserDao {
    void insertUser(User user);
    User getUser();
}
