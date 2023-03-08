package com.jy.springfox3.plus.example.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.jy.springfox3.plus.core.annotation.ApiModelPropertyPlus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel("基础实体")
public class BaseModel {
    /**
     * 主键
     */
    @ApiModelProperty("主键ID")
    @JsonView(BaseResponseView.class)
    protected String id;
    /**
     * 租户ID
     */
    @ApiModelProperty("租房ID")
    @ApiModelPropertyPlus({"tenantId"})
    @JsonView(BaseResponseView.class)
    protected Long tenantId;
    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    @ApiModelPropertyPlus({"creator"})
    @JsonView(BaseResponseView.class)
    protected LocalDateTime createTime;
    /**
     * 最后更新时间
     */
    @ApiModelProperty("更新时间")
    @ApiModelPropertyPlus({"updater"})
    @JsonView(BaseResponseView.class)
    protected LocalDateTime updateTime;
    /**
     * 创建者，目前使用 SysUser 的 id 编号
     *
     * 使用 String 类型的原因是，未来可能会存在非数值的情况，留好拓展性。
     */
    @ApiModelProperty("创建人")
    @ApiModelPropertyPlus({"creator"})
    @JsonView(BaseResponseView.class)
    protected String creator;
    /**
     * 更新者，目前使用 SysUser 的 id 编号
     *
     * 使用 String 类型的原因是，未来可能会存在非数值的情况，留好拓展性。
     */
    @ApiModelProperty("修改人")
    @ApiModelPropertyPlus({"updater"})
    @JsonView(BaseResponseView.class)
    protected String updater;
    /**
     * 是否删除
     */
    @ApiModelProperty("删除标志")
    @ApiModelPropertyPlus({"deleted"})
    @JsonView(BaseResponseView.class)
    protected Boolean deleted;

    public interface BaseResponseView{}
}
