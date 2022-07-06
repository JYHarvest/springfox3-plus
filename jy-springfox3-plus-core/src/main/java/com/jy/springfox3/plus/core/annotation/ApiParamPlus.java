package com.jy.springfox3.plus.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;

/**
 * @author qcsj
 * @since 2022/7/5
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiParamPlus {

    @AliasFor("group")
    String[] value() default {};

    @AliasFor("value")
    String[] group() default {};
}
