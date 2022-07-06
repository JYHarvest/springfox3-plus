package com.jy.springfox3.plus.core.plugin;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Lists;
import com.jy.springfox3.plus.core.annotation.ApiModelPropertyPlus;
import com.jy.springfox3.plus.core.annotation.ApiParamPlus;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import springfox.documentation.oas.web.OpenApiTransformationContext;
import springfox.documentation.oas.web.WebMvcOpenApiTransformationFilter;
import springfox.documentation.spi.DocumentationType;

/**
 * @author qcsj
 * @since 2022/7/6
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class WebMvcOpenApiTransformationPlusFilter implements WebMvcOpenApiTransformationFilter {

    @Resource
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Override
    public OpenAPI transform(OpenApiTransformationContext<HttpServletRequest> context) {
        List<Context> contextList = getContext();
        // 缓存现有的Schema
        OpenAPI openApi = context.getSpecification();
        Paths paths = openApi.getPaths();
        Components components = openApi.getComponents();
        Map<String, Schema> schemas = components.getSchemas();

        Map<String, Schema> schemasClone = new HashMap<>();
        List<String> modleNameList = contextList.stream()
                .map(Context::getModelName)
                .distinct()
                .collect(Collectors.toList());
        modleNameList.forEach(modelName -> {
            Schema schema = schemas.get(modelName);
            schemasClone.put(modelName, schema);
        });

        // 需要新增的schema
        Map<String, Schema> schemasNew = new HashMap<>();
        Map<String, Context> contextMap = contextList.stream()
                .collect(Collectors.toMap(Context::getPath, Function.identity()));
        paths.forEach((path, value) -> {
            RequestBody requestBody = getRequestBody(value);
            if (requestBody == null) {
                return;
            }
            Content content = requestBody.getContent();
            content.forEach((type, mediaType) -> {
                String ref = mediaType.getSchema().get$ref();
                Context txt = contextMap.get(path);
                if (txt == null) {
                    return;
                }
                String modelName = txt.getModelName();
                Schema schema = schemasClone.get(modelName);
                if (schema == null) {
                    return;
                }
                String modelNameNew = txt.getModelNameNew();
                List<String> invalidFieldNameList = txt.getInvalidFieldNameList();
                // 更新链接
                mediaType.getSchema().set$ref(ref.replace(modelName, modelNameNew));
                // 克隆
                Schema clone = JSONUtil.toBean(JSONUtil.toJsonStr(schema), Schema.class);
                Map propertiesOld = clone.getProperties();
                invalidFieldNameList.forEach(propertiesOld::remove);
                schemasNew.put(modelNameNew, clone);
            });
        });
        schemas.putAll(schemasNew);
        return openApi;
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }

    private RequestBody getRequestBody(PathItem pathItem) {
        Operation post = pathItem.getPost();
        Operation put = pathItem.getPut();
        if (post != null) {
            return post.getRequestBody();
        }
        if (put != null) {
            return put.getRequestBody();
        }
        return null;
    }

    private List<Context> getContext() {
        List<Context> contextList = Lists.newArrayList();
        if (requestMappingHandlerMapping == null) {
            return contextList;
        }
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping
                .getHandlerMethods();
        handlerMethods.forEach((k, v) -> {
            Set<String> pathList = k.getPatternsCondition().getPatterns();
            MethodParameter[] methodParameters = v.getMethodParameters();
            for (MethodParameter methodParameter : methodParameters) {
                // 有RequestBody和Group注解的方法参数
                if (methodParameter.hasParameterAnnotation(ApiParamPlus.class) && methodParameter
                        .hasParameterAnnotation(
                                org.springframework.web.bind.annotation.RequestBody.class)) {
                    ApiParamPlus apiParamPlus = methodParameter
                            .getParameterAnnotation(ApiParamPlus.class);
                    String[] groupArr = apiParamPlus.value();
                    Class<?> parameterType = methodParameter.getParameterType();
                    String simpleName = parameterType.getSimpleName();
                    // 入参名拼接分组
                    String simpleNameNew = buildModelName(methodParameter, apiParamPlus);
                    // 判断group，缓存无效字段名
                    List<String> invalidFieldNameList = new ArrayList<>();
                    Field[] fields = ReflectUtil.getFields(parameterType);
                    for (Field field : fields) {
                        Annotation[] annotations = AnnotationUtil.getAnnotations(field, false);
                        ApiModelPropertyPlus annotation = AnnotationUtil
                                .getAnnotation(field, ApiModelPropertyPlus.class);
                        if (annotation != null) {
                            String[] fieldGroup = annotation.value();
                            if (ArrayUtil.isNotEmpty(fieldGroup) && !ArrayUtil
                                    .containsAny(groupArr, fieldGroup)) {
                                invalidFieldNameList.add(field.getName());
                            }
                            String[] exclude = annotation.exclude();
                            if (ArrayUtil.isNotEmpty(exclude) && ArrayUtil
                                    .containsAny(groupArr, exclude)) {
                                invalidFieldNameList.add(field.getName());
                            }
                        }
                    }
                    pathList.forEach(path -> {
                        contextList.add(Context.builder()
                                .path(path)
                                .modelName(simpleName)
                                .modelNameNew(simpleNameNew)
                                .invalidFieldNameList(invalidFieldNameList)
                                .build());
                    });
                }

            }
        });
        return contextList;
    }

    private String buildModelName(MethodParameter methodParameter, ApiParamPlus apiParamPlus) {
        String[] groupArr = apiParamPlus.value();
        String suffixes = ArrayUtil.join(groupArr, "_");
        Class<?> parameterType = methodParameter.getParameterType();
        String simpleName = parameterType.getSimpleName();
        return simpleName + "_" + suffixes;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    private static class Context {

        private String path;
        private String modelName;
        private String modelNameNew;
        private List<String> invalidFieldNameList;
    }
}
