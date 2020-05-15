package com.spring.annotation;

import java.lang.annotation.*;

//注解在成员变量使用
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowried {
}
