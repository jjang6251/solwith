package com.example.solwith.aop;

import java.lang.annotation.*;


//커스텀 어노테이션
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecutionTime {
}
