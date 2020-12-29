package com.efreight.ws.hrs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("hrs_org_api_config")
public class WSAPIConfig implements Serializable {

    @TableId(value = "org_api_config_id", type = IdType.AUTO)
    private Integer orgApiConfigId;
    private Integer orgId;
    private String apiType;
    private String authToken;
}
