package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_tact_publish_price")
public class TactPublic implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(value = "publish_price_id", type = IdType.AUTO)
    private Integer publishPriceId;

    private Integer orgId;

    private String productName;

    private LocalDateTime beginDate;

    private LocalDateTime endDate;

    private String departureStation;

    private String arrivalStation;

    private String transitStation;

    private String carrierCode;

    private String goodsType;

    private String densityClass;

    @TableField("tact_m")
    private BigDecimal tactM;
    @TableField("tact_n")
    private BigDecimal tactN;
    @TableField("tact_45")
    private BigDecimal tact45;
    @TableField("tact_100")
    private BigDecimal tact100;
    @TableField("tact_300")
    private BigDecimal tact300;
    @TableField("tact_500")
    private BigDecimal tact500;
    @TableField("tact_700")
    private BigDecimal tact700;
    @TableField("tact_1000")
    private BigDecimal tact1000;
    @TableField("tact_2000")
    private BigDecimal tact2000;
    @TableField("tact_3000")
    private BigDecimal tact3000;
    @TableField("tact_5000")
    private BigDecimal tact5000;

    private String tactRemark;

    private Integer creatorId;

    private String creatorName;

    private LocalDateTime createTime;

    private Integer editorId;

    private String editorName;

    private LocalDateTime editTime;
    private transient String beginDateStr;
    private transient String endDateStr;
    private transient String operator;
    private transient String operatTime;
    private transient String flightDate;
    private transient String columnStrs;
}