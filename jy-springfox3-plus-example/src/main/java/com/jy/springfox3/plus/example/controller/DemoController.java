package com.jy.springfox3.plus.example.controller;

import com.jy.springfox3.plus.core.annotation.ApiParamPlus;
import com.jy.springfox3.plus.example.param.SpringFoxRequest;
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

    @GetMapping("getMethod1")
    @ApiOperation("获取列表")
    public String get1(@ApiParamPlus("1") SpringFoxRequest springFoxRequest) {
        return "get1";
    }

    @GetMapping("getMethod2")
    @ApiOperation("分页查询")
    public String get2(@ApiParamPlus("2") SpringFoxRequest springFoxRequest) {
        return "get2";
    }

    @PostMapping({"postMethod1", "postMethod111"})
    @ApiOperation("新增")
    public String post1(@ApiParamPlus("2") @RequestBody SpringFoxRequest request) {
        return "post1";
    }

    @PostMapping("postMethod2")
    @ApiOperation("修改")
    public String post2(@ApiParamPlus("1") @RequestBody SpringFoxRequest request) {
        return "post2";
    }

}
