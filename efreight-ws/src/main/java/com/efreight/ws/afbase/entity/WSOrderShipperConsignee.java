package com.efreight.ws.afbase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_order_shipper_consignee")
public class WSOrderShipperConsignee implements Serializable {

    @TableId(value = "order_sc_id", type = IdType.AUTO)
    private Integer orderScId;
    private Integer orgId;
    private Integer orderId;
    private Integer scType;
    private String scName;
    private String scPrintRemark;
    private Integer creatorId;
    private String creatorName;
    private LocalDateTime createTime;
    private Integer editorId;
    private String editorName;
    private LocalDateTime editTime;

 }
