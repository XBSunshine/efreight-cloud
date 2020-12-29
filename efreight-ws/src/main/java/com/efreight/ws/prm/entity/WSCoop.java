package com.efreight.ws.prm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("prm_coop")
public class WSCoop {
    private Integer coopId;
    private String coopCode;
    private Integer coopStatus;
    private Integer orgId;
}
