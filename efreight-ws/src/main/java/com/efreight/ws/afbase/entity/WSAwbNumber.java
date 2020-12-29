package com.efreight.ws.afbase.entity;

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
@TableName("af_awb_number")
public class WSAwbNumber implements Serializable {

    @TableId(value = "awb_id", type = IdType.AUTO)
    private Integer awbId;
    private String awbUuid;
    private Integer orgId;
    private String awbNumber;
    private String awbStatus;
}
