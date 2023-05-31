package com.jy.springfox3.plus.example.model;

import com.jy.springfox3.plus.core.annotation.ApiModelPropertyPlus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author qcsj
 * @since 2022/7/5
 */
@Getter
@Setter
@ApiModel("SpringFoxRequest")
public class SpringFoxRequest {

    @ApiModelPropertyPlus({"get", "modify"})
    @ApiModelProperty("id")
    private Long id;

    @ApiModelPropertyPlus({"create"})
    @ApiModelProperty("名称")
    private String name;

    @ApiModelPropertyPlus({"modify", "get"})
    @ApiModelProperty("年龄")
    private String age;

    @ApiModelPropertyPlus("modify")
    @ApiModelProperty("子对象")
    private SpringFoxChildRequest springFoxChildRequest;

}
