package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
public class Inbound implements Serializable {
    private static final long serialVersionUID = 1L;


    //主单入库操作ID
    @TableId(value = "inbound_id", type = IdType.AUTO)
    private Integer inboundId;

    //签约公司ID
    private Integer orgId;

    //订单
    private Integer orderId;

    //订单UUID
    private String orderUuid;

    //主单ID
    private Integer awbId;

    //主单UUID
    private String awbUuid;

    //订单件数
    private Integer orderPieces;

    //订单毛重
    private Double orderGrossWeight;

    //订单体积
    private Double orderVolume;

    //订单计费重量
    private Double orderChargeWeight;

    //订单尺寸
    private String orderSize;

    //订单照片文件名
    private String orderFileName;

    //订单照片URL
    private String orderFileUrl;

    //主单件数
    private Integer awbPieces;

    //主单毛重
    private Double awbGrossWeight;

    //主单体积
    private Double awbVolume;

    //主单计费重量
    private Double awbChargeWeight;

    //密度
    private Integer orderDimensions;

    //入库件数
    @TableField(exist = false)
    private Integer inboundPieces;

    //入库毛重
    @TableField(exist = false)
    private Double inboundGrossWeight;

    //入库体积
    @TableField(exist = false)
    private Double inboundVolume;

    //入库体积重量
    @TableField(exist = false)
    private Double inboundVolumeWeight;

    //入库计费重量
    @TableField(exist = false)
    private Double inboundChargeWeight;

    //主单号
    @TableField(exist = false)
    private String awbNumber;

    //订单号
    @TableField(exist = false)
    private String orderCode;

    //始发港
    @TableField(exist = false)
    private String departureStation;

    //出重开始日期
    @TableField(exist = false)
    private LocalDateTime inboundDateStart;

    //出重结束日期
    @TableField(exist = false)
    private LocalDateTime inboundDateEnd;

    //出重状态
    @TableField(exist = false)
    private String inboundStatus;

    //客户单号
    @TableField(exist = false)
    private String customerNumber;

    //是否是主单
    @TableField(exist = false)
    private Boolean ifAwb = true;

    private Integer creatorId;

    private String creatorName;

    private LocalDateTime createTime;

    private Integer editorId;

    private String editorName;

    private LocalDateTime editTime;

    /**
     * 航线签单
     */
    @TableField(exist = false)
    private Integer rountingSign;

    /**
     * 航线签单-服务产品
     */
    @TableField(exist = false)
    private String rountingSignBusinessProduct;

}
