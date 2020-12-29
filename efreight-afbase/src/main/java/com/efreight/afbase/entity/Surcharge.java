package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_awb_surcharge")
public class Surcharge implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 附加费ID
     */
    @TableId(value = "surcharge_id", type = IdType.AUTO)
    private Integer surchargeId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 航司ID
     */
    private Integer carrierId;

    /**
     * 始发港
     */
    private String departureStation;
    private transient List<String> departureStations;

    /**
     * 目的港
     */
    private String arrivalStation;
    private transient List<String> arrivalStations;

    /**
     * 目的港
     */
    private String arrivalNationCode;
    private transient List<String> arrivalNationCodes;

    /**
     * 航线分区
     */
    private String routingName;

    /**
     * 运单排序
     */
    private String awbSort;

    /**
     * 附加费名称
     */
    private String surchargeName;

    /**
     * 附加费代码
     */
    private String surchargeCode;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 收费方式
     */
    private String chargeMethod;

    /**
     * 最低收费
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal chargeMin;

    /**
     * 最高收费
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal chargeMax;

    /**
     * 生效日期
     */
    private LocalDateTime beginDate;

    /**
     * 失效日期
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDateTime endDate;

    private Integer creatorId;
    private String creatorName;
    private LocalDateTime createTime;
    private Integer editorId;
    private String editorName;
    private LocalDateTime editTime;

    private transient String createTimeBegin;
    private transient String nationCode;
    private transient String carrierCode;
    private transient String awbNumberPrefix;
    private transient String flightDate;
    @TableField(exist = false)
    private String columnStrs;

}
