package com.web.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
public class User {

    private String id;

    private String name;

    private int age;
}
