package com.jy.springfox3.plus.core;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * @author qcsj
 * @since 2022/7/5
 */
@Slf4j
@Configuration
@ComponentScan
public class SpringfoxPlusConfiguration {

    @Value("${springfox3-plus.documentation.baseUrl:}")
    private String swaggerBaseUrl;

    @Value("${springfox3-plus.documentation.enabled:true}")
    private boolean enabled;

    @Value("${springfox3-plus.documentation.groupName:default}")
    private String groupName;

    @Bean
    @ConditionalOnMissingBean(Docket.class)
    public Docket docketApi() {
        log.info(
                "\n\t------------------------------------"
                        + "\n\tSpringfox3 plus url: {}"
                        + "\n\t------------------------------------",
                swaggerBaseUrl + "/doc.html");
        return new Docket(DocumentationType.OAS_30)
                .enable(enabled)
                .groupName(groupName)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("接口文档")
                .description("用于描述接口用途等信息")
                .contact(new Contact("", "", ""))
                .version("1.0")
                .build();
    }

}
