package com.web.service;

import com.web.entity.User;

public interface UserService {
    void insertUser(User user);
    User getUser();
}
