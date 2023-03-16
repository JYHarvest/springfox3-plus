package com.jy.springfox3.plus.example.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.jy.springfox3.plus.core.annotation.ApiModelPropertyPlus;

/**
 * @author qcsj
 * @since 2022/7/5
 */
@ApiModel("SpringFoxRequest")
public class SpringFoxRequest {

    @ApiModelPropertyPlus({"get", "modify"})
    @ApiModelProperty("id")
    public Long id;

    @ApiModelPropertyPlus({"create"})
    @ApiModelProperty("名称")
    public String name;

    @ApiModelPropertyPlus({"modify","get"})
    @ApiModelProperty("年龄")
    public String age;

    @ApiModelPropertyPlus("modify")
    @ApiModelProperty("子对象")
    public SpringFoxChildRequest springFoxChildRequest;

}
