package com.web.controller;

import com.spring.annotation.Autowried;
import com.spring.annotation.Controller;
import com.spring.annotation.RequestMapping;
import com.spring.annotation.ResponseBody;
import com.web.entity.User;
import com.web.service.UserService;

/**
 * 由于Reflections.getMethodParamNames参数解析问题,Controller中方法参数只能定义为String类型,
 * 并且方法里还不能定义变量,否则会被解析认为是方法参数.
 */
@Controller("user")
@RequestMapping("api-v1/user")
public class UserController {

    @Autowried
    private UserService userService;

    @Autowried
    private UserService userCacheService;

    @RequestMapping("get")
    @ResponseBody
    public User getUser(String id) {
        return userService.getUser(id);
    }

    @RequestMapping("insert")
    @ResponseBody
    public void insertUser(String name, String age) {
        userService.insertUser(
                new User(
                        String.valueOf((int) (Math.random() * (9999 - 1 + 1) + 1)),
                        name,
                        Integer.parseInt(age)));
    }

    @RequestMapping("getByCache")
    @ResponseBody
    public User getUserByCache(String id) {
        return userCacheService.getUser(id);
    }

    @RequestMapping("insertToCache")
    @ResponseBody
    public void insertUserToCache(String name, String age) {
        userCacheService.insertUser(
                new User(
                        String.valueOf((int) (Math.random() * (9999 - 1 + 1) + 1)),
                        name,
                        Integer.parseInt(age)));
    }
}
