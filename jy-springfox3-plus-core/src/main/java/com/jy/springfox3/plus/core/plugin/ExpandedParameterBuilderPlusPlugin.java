package com.jy.springfox3.plus.core.plugin;

import cn.hutool.core.lang.Filter;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ParameterMetadataAccessor;
import springfox.documentation.spi.service.contexts.ParameterExpansionContext;

/**
 * 根据field在model中位置，在doc.html中显示；父类在前，子类在后；必填在前，非必填在后；
 *
 * @author qcsj
 * @since 2022/5/31
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@SuppressWarnings("deprecation")
public class ExpandedParameterBuilderPlusPlugin implements
        springfox.documentation.spi.service.ExpandedParameterBuilderPlugin {

    @Override
    public void apply(ParameterExpansionContext context) {
        ParameterMetadataAccessor metadataAccessor = (ParameterMetadataAccessor) ReflectUtil
                .getFieldValue(context, "metadataAccessor");
        if (metadataAccessor == null) {
            return;
        }
        List<AnnotatedElement> annotatedElements = (List<AnnotatedElement>) ReflectUtil
                .getFieldValue(metadataAccessor, "annotatedElements");
        if (annotatedElements == null) {
            return;
        }
        if (annotatedElements.size() < 2) {
            return;
        }
        Field field = (Field) annotatedElements.get(1);
        Class<?> declaringClass = field.getDeclaringClass();
        int i = indexInClass(declaringClass, field);
        context.getRequestParameterBuilder().parameterIndex(i);
    }


    private int indexInClass(Class<?> declaringClass, Field field) {
        Filter<Field> fieldFilter = ele -> {
            return !Modifier.isStatic(ele.getModifiers());
        };
        int startIndex = getStartIndex(declaringClass, fieldFilter);
        Field[] fields = getSelfFields(declaringClass, fieldFilter);
        return ArrayUtil.indexOf(fields, field) + startIndex;
    }

    private int getStartIndex(Class<?> declaringClass, Filter<Field> fieldFilter) {
        Class<?> superclass = declaringClass.getSuperclass();
        int startIndex = 0;
        while (superclass != null && superclass != Object.class) {
            Field[] fields = getSelfFields(superclass, fieldFilter);
            superclass = superclass.getSuperclass();
            startIndex += fields.length;
        }
        return startIndex;
    }

    private static Field[] getSelfFields(Class<?> beanClass, Filter<Field> fieldFilter) {
        return ArrayUtil.filter(ReflectUtil.getFieldsDirectly(beanClass, false), fieldFilter);
    }

    @Override
    public boolean supports(DocumentationType documentationType) {
        return true;
    }
}
