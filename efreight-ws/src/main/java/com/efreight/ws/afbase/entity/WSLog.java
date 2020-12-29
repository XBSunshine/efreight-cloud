package com.efreight.ws.afbase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_log")
public class WSLog {
    @TableId(value = "log_id",type = IdType.AUTO)
    private Integer logId;
    private Integer orgId;
    private String businessScope;
    private String logType;
    private String nodeName;
    private String pageName;
    private String pageFunction;
    private String logRemark;
    private String projectUuid;
    private String projectName;
    private String orderUuid;
    private Integer orderId;
    private String orderNumber;
    private String awbUuid;
    private String awbNumber;
    private Integer creatorId;
    private String creatorName;
    private LocalDateTime creatTime;
}
