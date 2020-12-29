package com.efreight.sc.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * LC 陆运订单： 操作出重表
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("lc_inbound")
public class LcInbound implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 作操作出重ID
     */
    @TableId(value = "inbound_id", type = IdType.AUTO)
    private Integer inboundId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 订单UUID
     */
    private String orderUuid;

    /**
     * 订单
     */
    private Integer orderId;

    /**
     * 订单件数
     */
    private Integer orderPieces;

    /**
     * 订单毛重
     */
    private BigDecimal orderGrossWeight;

    /**
     * 订单体积
     */
    private BigDecimal orderVolume;

    /**
     * 订单体积重量
     */
    @TableField(exist = false)
    private BigDecimal orderVolumeWeight;

    /**
     * 订单计费重量
     */
    private BigDecimal orderChargeWeight;

    /**
     * 订单密度
     */
    private Integer orderDimensions;

    /**
     * 订单尺寸
     */
    private String orderSize;

    private Integer creatorId;

    private String creatorName;

    private LocalDateTime createTime;

    private Integer editorId;

    private String editorName;

    private LocalDateTime editTime;

    @TableField(exist = false)
    private String rowUuid;
    @TableField(exist = false)
    private String orderCode;
    @TableField(exist = false)
    private String customerNumber;

}
