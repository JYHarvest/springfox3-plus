package com.jy.springfox3.plus.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author qcsj
 * @since 2022/7/5
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiModelPropertyPlus {

    @AliasFor("group")
    String[] value() default {};

    @AliasFor("value")
    String[] group() default {};


}
