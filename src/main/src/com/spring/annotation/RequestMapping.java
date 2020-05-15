package com.spring.annotation;

import java.lang.annotation.*;

//注解在类和方法使用
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {
    String value() default "";
}
