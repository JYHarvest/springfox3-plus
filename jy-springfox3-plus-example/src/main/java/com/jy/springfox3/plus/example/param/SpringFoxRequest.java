package com.jy.springfox3.plus.example.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.jy.springfox3.plus.core.annotation.ApiModelPropertyPlus;

/**
 * @author qcsj
 * @since 2022/7/5
 */
@ApiModel("SpringFoxRequest")
public class SpringFoxRequest {

    @ApiModelPropertyPlus({"1", "2"})
    @ApiModelProperty("id")
    public Long id;

    @ApiModelPropertyPlus("1")
    @ApiModelProperty("名称")
    public String name;

    @ApiModelPropertyPlus("2")
    @ApiModelProperty("年龄")
    public String age;

    @ApiModelPropertyPlus("2")
    @ApiModelProperty("子对象")
    public SpringFoxChildRequest springFoxChildRequest;

}
