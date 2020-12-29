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
@TableName("af_inbound")
public class WSInbound implements Serializable {
    @TableId(value = "inbound_id", type = IdType.AUTO)
    private Integer inboundId;

    private Integer orgId;

    private Integer orderId;

    private String orderUuid;

    private Integer orderPieces;

    private Double orderGrossWeight;

    private Double orderVolume;

    private Double orderChargeWeight;

    private Integer orderDimensions;

    private Integer creatorId;

    private String creatorName;

    private LocalDateTime createTime;

    private Integer editorId;

    private String editorName;

    private LocalDateTime editTime;
}
