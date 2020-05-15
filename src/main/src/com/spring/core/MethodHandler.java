package com.spring.core;


import lombok.Data;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 方法代理信息
 */
@Data
public class MethodHandler {

    //方法所在的类
    private Object object;

    //方法名称
    private Method method;

    //方法参数
    private List<String> params;
}
