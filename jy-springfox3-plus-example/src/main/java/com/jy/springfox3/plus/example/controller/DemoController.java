package com.jy.springfox3.plus.example.controller;

import com.jy.springfox3.plus.core.annotation.ApiParamPlus;
import com.jy.springfox3.plus.core.annotation.ApiResponseGroup;
import com.jy.springfox3.plus.example.model.SpringFoxRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author qcsj
 * @since 2022/7/6
 */
@Api(tags = "接口文档")
@RestController
@RequestMapping("springfox-plus")
public class DemoController {

    @GetMapping("list")
    @ApiOperation("获取列表")
    @ApiResponseGroup("get")
    public SpringFoxRequest list(@ApiParamPlus("get") SpringFoxRequest springFoxRequest) {//使用get请求,无法检测Bean属性
        SpringFoxRequest request = new SpringFoxRequest();
        request.name = "1";
        return request;
    }

    @GetMapping("page")
    @ApiOperation("分页查询")
    public String page(@ApiParamPlus("get") SpringFoxRequest springFoxRequest) {//使用get请求,无法检测Bean属性
        return "page";
    }

    @PostMapping({"create"})
    @ApiOperation("新增")
    public String post1(@ApiParamPlus("create") @RequestBody SpringFoxRequest request) {
        return "create";
    }

    @PostMapping("modify")
    @ApiOperation("修改")
    public String modify(@ApiParamPlus("modify") @RequestBody SpringFoxRequest request) {
        return "modify";
    }

}
