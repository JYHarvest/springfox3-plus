package com.jy.springfox3.plus.core.plugin;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import com.fasterxml.classmate.ResolvedType;
import com.jy.springfox3.plus.core.annotation.ApiModelPropertyPlus;
import com.jy.springfox3.plus.core.annotation.ApiParamPlus;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.builders.OperationBuilder;
import springfox.documentation.service.Operation;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;

/**
 * @author qcsj
 * @since 2022/7/5
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class DefaultOperationPlusReader implements OperationBuilderPlugin {

    @Override
    public void apply(OperationContext context) {
        Operation operation = context.operationBuilder().build();
        Set<RequestParameter> requestParameters = operation.getRequestParameters();
        Map<String, RequestParameter> filedNameMap = requestParameters.stream()
                .collect(Collectors.toMap(RequestParameter::getName, Function.identity()));

        List<ResolvedMethodParameter> methodParameters = context.getParameters();
        for (ResolvedMethodParameter methodParameter : methodParameters) {
            if (!methodParameter.hasParameterAnnotation(ApiParamPlus.class)) {
                continue;
            }
            Optional<ApiParamPlus> groupOptional = methodParameter
                    .findAnnotation(ApiParamPlus.class);
            String[] groupArr = groupOptional.get().value();
            ResolvedType parameterType = methodParameter.getParameterType();
            Class<?> erasedType = parameterType.getErasedType();
            Field[] fields = ReflectUtil.getFields(erasedType);
            for (Field field : fields) {
                boolean normalClass = ClassUtil.isSimpleValueType(field.getType());
                if (!normalClass) {
                    // todo 递归 对象属性
                }
                Annotation[] annotations = AnnotationUtil.getAnnotations(field, false);
                ApiModelPropertyPlus annotation = AnnotationUtil
                        .getAnnotation(field, ApiModelPropertyPlus.class);
                if (annotation != null) {
                    String[] fieldGroup = annotation.value();
                    if (!ArrayUtil.containsAny(groupArr, fieldGroup)) {
                        filedNameMap.remove(field.getName());
                    }
                }
            }
        }
        OperationBuilder operationBuilder = context.operationBuilder();
        ReflectUtil.setFieldValue(operationBuilder, "requestParameters", new HashSet<>());
        operationBuilder.requestParameters(filedNameMap.values());
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }
}
