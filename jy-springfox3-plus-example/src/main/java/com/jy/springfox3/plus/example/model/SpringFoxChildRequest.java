package com.jy.springfox3.plus.example.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.jy.springfox3.plus.core.annotation.ApiModelPropertyPlus;

/**
 * @author qcsj
 * @since 2022/7/5
 */
@ApiModel("关联类属性")
public class SpringFoxChildRequest extends BaseModel{

    @ApiModelPropertyPlus({"create","get"})
    @ApiModelProperty("名称1")
    public String name1;

    @ApiModelPropertyPlus({"create","modify","get"})
    @ApiModelProperty("年龄1")
    public String age1;
}
