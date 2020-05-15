package com.spring.annotation;

import java.lang.annotation.*;

//注解在方法或类上使用
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseBody {
}
