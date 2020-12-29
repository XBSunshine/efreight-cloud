package com.efreight.common.remoteVo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AfOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer orderId;
    /**
     * 订单uuid
     */
    private String orderUuid;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 订单号
     */
    private String orderCode;

    /**
     * 主单号
     */
    private String awbNumber;

    /**
     * 分单号
     */
    private String hawbNumber;

    /**
     * 客户单号
     */
    private String customerNumber;

    /**
     * 订单状态
     */
    private String orderStatus;

    private Boolean costRecorded;

    private String costStatus;


    /**
     * 预报件数
     */
    private Integer planPieces;

    /**
     * 预报毛重
     */
    private BigDecimal planWeight;

    /**
     * 预报体积
     */
    private Double planVolume;

    /**
     * 预报计费重量
     */
    private Double planChargeWeight;

    /**
     * 预报密度
     */
    private Integer planDensity;

    /**
     * 实际密度
     */
    private Integer confirmDensity;

    /**
     * 预报尺寸
     */
    private String planDimensions;

    /**
     * 实际件数
     */
    private Integer confirmPieces;

    /**
     * 实际毛重
     */
    private BigDecimal confirmWeight;

    /**
     * 实际体积
     */
    private Double confirmVolume;

    /**
     * 实际计费重量
     */
    private Double confirmChargeWeight;

}
