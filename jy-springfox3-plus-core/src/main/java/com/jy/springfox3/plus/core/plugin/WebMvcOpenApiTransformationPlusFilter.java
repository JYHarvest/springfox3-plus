package com.jy.springfox3.plus.core.plugin;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.lang.Tuple;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.collect.Lists;
import com.jy.springfox3.plus.core.annotation.ApiModelPropertyPlus;
import com.jy.springfox3.plus.core.annotation.ApiParamPlus;
import com.jy.springfox3.plus.core.annotation.ApiResponseGroup;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
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
public class WebMvcOpenApiTransformationPlusFilter implements WebMvcOpenApiTransformationFilter, InitializingBean {

    @Resource
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    private Map<String,List<Context>> pathContext = new ConcurrentHashMap<>();
    private List<Context> contexts = new ArrayList<>();

    @Override
    public OpenAPI transform(OpenApiTransformationContext<HttpServletRequest> context) {
        OpenAPI openApi = context.getSpecification();
        Paths paths = openApi.getPaths();
        Components components = openApi.getComponents();
        Map<String, Schema> defSchemas = components.getSchemas();
        Map<String, Schema> newSchemas = new HashMap<>();
        paths.forEach((path, value) -> {
            //获取POST/PUT请求参数中的body
            List<RequestBody> requestBodys = getRequestBody(value);
            requestBodys.forEach(requestBody -> {
                Content content = requestBody.getContent();
                content.forEach((type, mediaType) -> {
                    String ref = mediaType.getSchema().get$ref();
                    Context pathContext = getContext(path,ref);
                    if (pathContext == null) {
                        return;
                    }
                    String modelName = pathContext.getModelName();
                    Schema defSchema = defSchemas.get(modelName);
                    if (defSchema == null) {
                        return;
                    }
                    String modelNewName = pathContext.getModelNewName();
                    // 更新链接
                    mediaType.getSchema().set$ref(ref.replace(modelName, modelNewName));
                    if(!newSchemas.containsKey(modelNewName)) {
                        List<String> invalidFieldNameList = pathContext.getInvalidFieldNames();
                        // 克隆
                        Schema clone = JSONUtil.toBean(JSONUtil.toJsonStr(defSchema), Schema.class);
                        Map propertiesOld = clone.getProperties();
                        invalidFieldNameList.forEach(propertiesOld::remove);
                        newSchemas.put(modelNewName, clone);
                        resetPropertyRef(clone, path, newSchemas, defSchemas);
                    }
                });
            });
            //存在POST/PUT多个Mapping时就有多个Response
            List<ApiResponse> responses = getResponseBody(value);
            responses.forEach(response -> {
                Content content = response.getContent();
                content.forEach((type, mediaType) -> {
                    String ref = mediaType.getSchema().get$ref();
                    if(StringUtils.isEmpty(ref)) {
                        return;
                    }
                    Context pathContext = getContext(path,ref);
                    if (pathContext == null) {
                        return;
                    }
                    String modelName = pathContext.getModelName();
                    Schema schema = defSchemas.get(modelName);
                    if (schema == null) {
                        return;
                    }
                    String modelNewName = pathContext.getModelNewName();
                    // 更新链接
                    mediaType.getSchema().set$ref(ref.replace(modelName, modelNewName));
                    if(!newSchemas.containsKey(modelNewName)) {
                        List<String> invalidFieldNameList = pathContext.getInvalidFieldNames();
                        // 克隆
                        Schema clone = JSONUtil.toBean(JSONUtil.toJsonStr(schema), Schema.class);
                        Map propertiesOld = clone.getProperties();
                        invalidFieldNameList.forEach(propertiesOld::remove);
                        newSchemas.put(modelNewName, clone);
                    }
                });
            });
        });
        defSchemas.putAll(newSchemas);
        return openApi;
    }
    //出现循环属性时,只需出现一个就不再处理
    private void resetPropertyRef(Schema schema,String path,Map<String, Schema> newSchemas,Map<String, Schema> defSchemas) {
        Map<String,Schema> properties = schema.getProperties();
        properties.forEach((k,propSchema)->{
            if(!StringUtils.isEmpty(propSchema.get$ref())) {
                Context context = getContext(path, propSchema.get$ref());
                if(context != null) {
                    String modelNewName = context.getModelNewName();
                    if(!newSchemas.containsKey(modelNewName)) {//判断是否循环属性,不是循环属性则递归下层属性
                        Schema defSchema = defSchemas.get(context.getModelName());
                        if (defSchema == null) {
                            return;
                        }
                        Schema clone = JSONUtil.toBean(JSONUtil.toJsonStr(defSchema), Schema.class);
                        Map propertiesOld = clone.getProperties();
                        context.getInvalidFieldNames().forEach(propertiesOld::remove);
                        propSchema.set$ref(propSchema.get$ref().replace(context.getModelName(),modelNewName));
                        newSchemas.put(context.getModelNewName(),clone);
                        resetPropertyRef(clone,path,newSchemas,defSchemas);
                    } else {
                        //循环属性时只处理引用
                        propSchema.set$ref(propSchema.get$ref().replace(context.getModelName(),modelNewName));
                    }
                }
            }
        });
    }

    private Context getContext(String path,String ref) {
        if(StringUtils.isEmpty(path) || StringUtils.isEmpty(ref)) {
            return null;
        }
        List<Context> Contexts = pathContext.get(path);
        if(Contexts != null) {
            String modelName = ref.substring(ref.lastIndexOf("/")+1);
            for (Context Context : Contexts) {
                if(Context.modelName.equals(modelName)) {
                    return Context;
                }
            }
        }
        return null;
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }

    private List<RequestBody> getRequestBody(PathItem pathItem) {
        List<RequestBody> bodys = new ArrayList<>();
        Operation post = pathItem.getPost();
        Operation put = pathItem.getPut();
        if (post != null) {
            bodys.add(post.getRequestBody());
        }
        if (put != null) {
            bodys.add(put.getRequestBody());
        }
        return bodys;
    }

    private List<ApiResponse> getResponseBody(PathItem pathItem) {
        List<ApiResponse> responses = new ArrayList<>();
        Operation operation = pathItem.getGet();
        if(operation != null) {
            responses.add(operation.getResponses().get("200"));
        }
        operation = pathItem.getPut();
        if(operation != null) {
            responses.add(operation.getResponses().get("200"));
        }
        operation = pathItem.getPost();
        if(operation != null) {
            responses.add(operation.getResponses().get("200"));
        }
        operation = pathItem.getDelete();
        if(operation != null) {
            responses.add(operation.getResponses().get("200"));
        }
        return responses;
    }


    private void initContext() {
        if(requestMappingHandlerMapping == null) {
            return ;
        }
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping
                .getHandlerMethods();
        handlerMethods.forEach((k, v) -> {
            //可能配置了多个GET/POST/PUT
            Set<String> pathList = k.getPatternsCondition().getPatterns();
            MethodParameter[] methodParameters = v.getMethodParameters();
            Api apiAnnatation = v.getBeanType().getAnnotation(Api.class);
            String beanApiName = v.getBeanType().getSimpleName();
            if(apiAnnatation != null) {
                String[] tags = apiAnnatation.tags();
                if(ArrayUtil.isNotEmpty(tags)) {
                    beanApiName = tags[0];
                }else if(!StringUtils.isEmpty(apiAnnatation.value())) {
                    beanApiName = apiAnnatation.value();
                }
            }
            for (MethodParameter methodParameter : methodParameters) {
                // 有RequestBody和Group注解的方法参数
                if (methodParameter.hasParameterAnnotation(ApiParamPlus.class) && methodParameter
                        .hasParameterAnnotation(
                                org.springframework.web.bind.annotation.RequestBody.class)) {
                    //请求参数上的@ApiParamPlus注解
                    ApiParamPlus ApiParamPlus = methodParameter.getParameterAnnotation(ApiParamPlus.class);
                    //请求参数的组
                    String[] parameterGroup = ApiParamPlus.value();
                    //请求参数的Class类型
                    Class<?> parameterType = methodParameter.getParameterType();
                    //定义在方法上的api名称
                    ApiOperation apiOperation = methodParameter.getMethodAnnotation(ApiOperation.class);
                    List<Context> Contexts = new ArrayList<>();
                    String methodApiName = "";
                    if(apiOperation != null) {
                        methodApiName = apiOperation.value();
                    }
                    List<String> existNames = new ArrayList<>();
                    buildContext(Contexts,parameterGroup,beanApiName,methodApiName,parameterType,existNames,ApiTypeEnum.REQUEST,null);
                    if(Contexts != null && Contexts.size() > 0) {
                        pathList.forEach(path -> {
                            pathContext.put(path, Contexts);
                        });
                        contexts.addAll(Contexts);
                    }
                }
            }
            MethodParameter returnParameter = v.getReturnType();
            if(returnParameter != null && returnParameter.hasMethodAnnotation(ApiResponseGroup.class)) {
                String suffixName = "";
                if(v.hasMethodAnnotation(JsonView.class)) {
                    JsonView methodAnnotation = v.getMethodAnnotation(JsonView.class);
                    Class<?>[] viewClass = methodAnnotation.value();
                    if(!ObjectUtils.isEmpty(viewClass)) {
                        suffixName = viewClass[0].getSimpleName() +"View";
                    }
                }
                ApiResponseGroup apiResponseGroup = returnParameter.getMethodAnnotation(ApiResponseGroup.class);
                String[] returnGroup = apiResponseGroup.value();
                Class<?> returnType = returnParameter.getParameterType();
                ApiOperation apiOperation = returnParameter.getMethodAnnotation(ApiOperation.class);
                List<Context> Contexts = new ArrayList<>();
                String methodApiName = "";
                if(apiOperation != null) {
                    methodApiName = apiOperation.value();
                }
                List<String> existNames = new ArrayList<>();
                buildContext(Contexts,returnGroup,beanApiName,methodApiName,returnType,existNames,ApiTypeEnum.RESPONSE,suffixName);
                if(Contexts != null && Contexts.size() > 0) {
                    pathList.forEach(path -> {
                        List<Context> ContextsExist = pathContext.get(path);
                        if(ContextsExist == null) {
                            pathContext.put(path, Contexts);
                        }else {
                            ContextsExist.addAll(Contexts);
                        }
                    });
                    contexts.addAll(Contexts);
                }
            }
        });
    }

    private void buildContext(List<Context> Contexts,String[] parameterGroup,String beanApiName,String methodApiName,Class<?> fieldClass,List<String> existNames,ApiTypeEnum apiType,String suffixName) {
        if(existNames.contains(fieldClass.getName())) {
            return ;
        }
        //判断该类是否注解了ApiModel，有的话按配置的名称作为属性名，默认为类的名称
        ApiModel apiModel = fieldClass.getAnnotation(ApiModel.class);
        //类名称
        String modelName = fieldClass.getSimpleName();
        if(apiModel != null) {
            //参数类上的api名称
            modelName = apiModel.value();
        }
        if(!StringUtils.isEmpty(suffixName)) {
            modelName += suffixName;
        }
        String groupName = ArrayUtil.join(parameterGroup, "_");
        String modelNewName = groupName;
        //定义在方法上的api名称
        if(!StringUtils.isEmpty(methodApiName)) {
            modelNewName = beanApiName+"."+methodApiName +"."+modelName;
        }
        if(apiType.equals(ApiTypeEnum.RESPONSE)) {
            modelNewName += "."+apiType.modelType;
        }
        Field[] fields = ReflectUtil.getFields(fieldClass);
        List<String> invalidFieldName = new ArrayList<>();
        existNames.add(fieldClass.getName());
        for (Field field : fields) {
            ApiModelPropertyPlus annotation = AnnotationUtil.getAnnotation(field, ApiModelPropertyPlus.class);
            if (annotation != null) {
                boolean isInvalid = false;
                String[] fieldGroup = annotation.value();
                if (ArrayUtil.isNotEmpty(fieldGroup) && !ArrayUtil.containsAny(parameterGroup, fieldGroup)) {
                    invalidFieldName.add(field.getName());
                    isInvalid = true;
                }
                String[] exclude = annotation.exclude();
                if (ArrayUtil.isNotEmpty(exclude) && ArrayUtil.containsAny(parameterGroup, exclude)) {
                    invalidFieldName.add(field.getName());
                    isInvalid = true;
                }
                if(!isInvalid) {
                    boolean normalClass = ClassUtil.isSimpleValueType(field.getType());
                    if (!normalClass) {
                        buildContext(Contexts,parameterGroup,beanApiName,methodApiName,field.getType(),existNames,apiType,suffixName);
                    }
                }
            }
        }
        Contexts.add(Context.builder()
                .apiType(apiType)
                .className(fieldClass.getName())
                .groupName(groupName)
                .invalidFieldNames(invalidFieldName)
                .modelName(modelName)//原来的名称(ApiModel名称,JsonView时需要ApiModel名称+JsonView组名称+View)
                .modelNewName(modelNewName)//controller注解名称-方法注释名称
                .build());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initContext();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    private static class Context {
        private String modelName;//openApi上的名称
        private String modelNewName;//新的名称
        private List<String> invalidFieldNames;//该组中无效的字段名
        private ApiTypeEnum apiType;//请求方式，response,request
        private String groupName;//定义在该请求参数上的组列表
        private String className;//参数的类(包名+类名)
    }
    //api类型枚举,用于区分是请求还是响应
    private enum ApiTypeEnum {
        /** 请求类型 */
        REQUEST("request"),
        /** 响应类型 */
        RESPONSE("response");
        private String modelType;
        ApiTypeEnum(String modelType) {
            this.modelType = modelType;
        }
        public String getModelType() {
            return modelType;
        }
    }
}
