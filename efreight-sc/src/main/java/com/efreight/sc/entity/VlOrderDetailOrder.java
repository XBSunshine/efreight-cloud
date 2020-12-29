package com.efreight.sc.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.List;

import com.efreight.common.remoteVo.Service;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * VL 订单管理 派车订单
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("vl_order_detail_order")
public class VlOrderDetailOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 派车单订单明细ID
     */
    @TableId(value = "vl_detail_order_id", type = IdType.AUTO)
    private Integer vlDetailOrderId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 派车单订单ID
     */
    private Integer vlOrderId;

    /**
     * 业务订单ID
     */
    private Integer orderId;

    @TableField(exist = false)
    private String orderUuid;

    /**
     * 业务范畴
     */
    private String businessScope;

    /**
     * 成本服务ID
     */
    private Integer serviceId;

    /**
     * 成本服务名称
     */
    private String serviceName;

    @TableField(exist = false)
    private List<Service> services;

    /**
     * 分摊成本：金额
     */
    private BigDecimal costAmount;

    @TableField(exist = false)
    private String costAmountStr;

    /**
     * 运费成本：币种
     */
    private String costCurrecnyCode;

    /**
     * 分摊成本：成本表ID
     */
    private Integer costId;

    /**
     * 预报件数
     */
    private Integer planPieces;

    /**
     * 预报毛重
     */
    private BigDecimal planWeight;

    @TableField(exist = false)
    private String planWeightStr;

    /**
     * 预报体积
     */
    private Double planVolume;

    @TableField(exist = false)
    private String planVolumeStr;

    /**
     * 预报计费重量
     */
    private Double planChargeWeight;

    @TableField(exist = false)
    private String planChargeWeightStr;

    /**
     * 预报密度
     */
    private Integer planDensity;

    /**
     * 实际件数
     */
    private Integer confirmPieces;

    /**
     * 实际毛重
     */
    private BigDecimal confirmWeight;

    @TableField(exist = false)
    private String confirmWeightStr;

    /**
     * 实际体积
     */
    private Double confirmVolume;

    @TableField(exist = false)
    private String confirmVolumeStr;

    /**
     * 实际计费重量
     */
    private Double confirmChargeWeight;

    @TableField(exist = false)
    private String confirmChargeWeightStr;

    /**
     * 创建人
     */
    private Integer creatorId;

    private String creatorName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    private Integer editorId;

    private String editorName;

    /**
     * 修改时间
     */
    private LocalDateTime editTime;

    @TableField(exist = false)
    private String awbNumber;
    @TableField(exist = false)
    private String orderCode;
    @TableField(exist = false)
    private String customerNumber;

    @TableField(exist = false)
    private BigDecimal planChargeWeightNew;
    @TableField(exist = false)
    private Integer planDensityNew;
    @TableField(exist = false)
    private Integer planPiecesNew;
    @TableField(exist = false)
    private Double planVolumeNew;
    @TableField(exist = false)
    private BigDecimal planWeightNew;
    @TableField(exist = false)
    private Double confirmChargeWeightNew;
    @TableField(exist = false)
    private Integer confirmPiecesNew;
    @TableField(exist = false)
    private Double confirmVolumeNew;
    @TableField(exist = false)
    private BigDecimal confirmWeightNew;
    @TableField(exist = false)
    private String hawbNumber;


}
