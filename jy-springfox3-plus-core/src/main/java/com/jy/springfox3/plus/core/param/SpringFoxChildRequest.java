package com.jy.springfox3.plus.core.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.jy.springfox3.plus.core.annotation.ApiModelPropertyPlus;

/**
 * @author qcsj
 * @since 2022/7/5
 */
@ApiModel("SpringFoxChildRequest")
public class SpringFoxChildRequest {

    @ApiModelPropertyPlus({"1", "2"})
    @ApiModelProperty("id1")
    public String id1;

    @ApiModelPropertyPlus("1")
    @ApiModelProperty("名称1")
    public String name1;

    @ApiModelPropertyPlus("2")
    @ApiModelProperty("年龄1")
    public String age1;
}
