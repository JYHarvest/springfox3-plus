package com.jy.springfox3.plus.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;

/**
 * @author qcsj
 * @since 2022/7/5
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiModelPropertyPlus {

    /**
     * 默认值
     *
     * @return
     */
    @AliasFor("group")
    String[] value() default {};

    /**
     * 包含分组
     *
     * @return
     */
    @AliasFor("value")
    String[] group() default {};

    /**
     * 排除分组
     *
     * @return
     */
    String[] exclude() default {};

}
