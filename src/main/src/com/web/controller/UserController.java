package com.web.controller;

import com.spring.annotation.Autowried;
import com.spring.annotation.Controller;
import com.spring.annotation.RequestMapping;
import com.spring.annotation.ResponseBody;
import com.web.entity.User;
import com.web.service.UserService;

@Controller("user")
@RequestMapping("api-v1/user")
public class UserController {

    @Autowried
    private UserService userService;

    @RequestMapping("get")
    @ResponseBody
    public User getUser() {
        return userService.getUser();
    }

    @RequestMapping("insert")
    public void insertUser(String name, int age) {
        int id = (int) (Math.random() * (9999 - 1 + 1) + 1);
        User user = new User(String.valueOf(id), name, age);
        userService.insertUser(user);
    }
}
