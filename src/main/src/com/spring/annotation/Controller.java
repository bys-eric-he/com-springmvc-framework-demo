package com.spring.annotation;

import java.lang.annotation.*;

//注解在类上使用
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Controller {
    String value() default "";
}
