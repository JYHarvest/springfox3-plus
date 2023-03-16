package com.jy.springfox3.plus.core.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author qcsj
 * @since 2022/7/5
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiResponseGroup {

    @AliasFor("group")
    String[] value() default {};

    @AliasFor("value")
    String[] group() default {};
}
