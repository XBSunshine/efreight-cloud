package com.efreight.sc.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * <p>
 * LC 订单管理 LC陆运订单
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("lc_order")
public class LcOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单id
     */
    @TableId(value = "order_id", type = IdType.AUTO)
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
     * 订单状态
     */
    private String orderStatus;

    /**
     * 应收情况：未录收入、已录收入、已制账单、部分核销、核销完毕
     */
    private String incomeStatus;

    /**
     * 应付情况：未录成本、已录成本、已对账、部分核销、核销完毕
     */
    private String costStatus;

    /**
     * 收入完成：1是  0否
     */
    private Boolean incomeRecorded;

    /**
     * 成本完成：1是  0否
     */
    private Boolean costRecorded;

    @TableField(exist = false)
    private Integer incomeRecordedForSort;
    @TableField(exist = false)
    private Integer costRecordedForSort;

    /**
     * 客户单号
     */
    private String customerNumber;

    /**
     * 关联订单号
     */
    private String orderCodeAssociated;

    /**
     * 客户ID
     */
    private Integer coopId;

    @TableField(exist = false)
    private String coopName;

    /**
     * 业务范畴
     */
    private String businessScope;

    /**
     * 运输方式
     */
    private String shippingMethod;

    /**
     * 用车时间
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime drivingTime;
    @TableField(exist = false)
    private LocalDateTime drivingTimeStart;
    @TableField(exist = false)
    private LocalDateTime drivingTimeEnd;

    /**
     * 始发城市
     */
    private String departureStation;

    /**
     * 目的城市
     */
    private String arrivalStation;

    /**
     * 始发地址
     */
    private String departureAddress;

    /**
     * 目的地址
     */
    private String arrivalAddress;

    /**
     * 货物品名_中文
     */
    private String goodsNameCn;

    /**
     * 货物类型
     */
    private String goodsType;

    /**
     * 电池情况
     */
    private String batteryType;

    /**
     * 订单备注
     */
    private String orderRemark;

    /**
     * 预报件数
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer planPieces;

    @TableField(exist = false)
    private String planPiecesStr;

    /**
     * 预报毛重
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal planWeight;

    @TableField(exist = false)
    private String planWeightStr;

    /**
     * 预报体积
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal planVolume;

    @TableField(exist = false)
    private String planVolumeStr;

    /**
     * 预报计重
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal planChargeWeight;

    @TableField(exist = false)
    private String planChargeWeightStr;

    /**
     * 预报尺寸
     */
    private String planDimensions;

    /**
     * 预报密度
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer planDensity;

    /**
     * 实际件数
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer confirmPieces;

    @TableField(exist = false)
    private String confirmPiecesStr;

    /**
     * 实际毛重
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal confirmWeight;

    @TableField(exist = false)
    private String confirmWeightStr;

    /**
     * 实际体积
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal confirmVolume;

    @TableField(exist = false)
    private String confirmVolumeStr;

    /**
     * 实际计重
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal confirmChargeWeight;

    @TableField(exist = false)
    private String confirmChargeWeightStr;

    /**
     * 实际密度
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer confirmDensity;

    /**
     * 责任客服ID
     */
    private Integer servicerId;

    /**
     * 责任客服名称
     */
    private String servicerName;

    /**
     * 责任销售ID
     */
    private Integer salesId;

    /**
     * 责任销售名称
     */
    private String salesName;

    /**
     * 运费卖价：单价
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal freightUnitprice;

    /**
     * 运费卖价：总价
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal freightAmount;

    /**
     * 运费卖价：币种
     */
    private String freightCurrecnyCode;

    /**
     * 运费成本：单价
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal msrUnitprice;

    /**
     * 运费成本：总价
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal msrAmount;

    /**
     * 运费成本：币种
     */
    private String msrCurrecnyCode;

    /**
     * 价格备注
     */
    private String priceRemark;

    /**
     * 派车单号
     */
    private String orderCodeVl;

    /**
     * 车牌号
     */
    private String driverNumber;

    /**
     * 司机姓名
     */
    private String driverName;

    /**
     * 司机电话
     */
    private String driverTel;

    /**
     * 创建人
     */
    private Integer creatorId;

    private String creatorName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    @TableField(exist = false)
    private LocalDateTime createTimeStart;
    @TableField(exist = false)
    private LocalDateTime createTimeEnd;

    /**
     * 修改人
     */
    private Integer editorId;

    private String editorName;

    /**
     * 修改时间
     */
    private LocalDateTime editTime;

    /**
     * rowid
     */
    private String rowUuid;

    @TableField(exist = false)
    private String columnStrs;

    /**
     * 工作组ID
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer workgroupId;

    /**
     * 当前用户的订单权限
     */
    @TableField(exist = false)
    private Integer orderPermission;

    /**
     * 当前用户ID
     */
    @TableField(exist = false)
    private Integer currentUserId;


}
