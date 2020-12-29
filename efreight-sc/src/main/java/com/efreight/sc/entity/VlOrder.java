package com.efreight.sc.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
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
@TableName("vl_order")
public class VlOrder implements Serializable {

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
     * 订单状态：创建订单、完成订单、强制关闭
     */
    private String orderStatus;

    /**
     * 客户ID：车队
     */
    private Integer coopId;

    @TableField(exist = false)
    private String coopName;

    /**
     * 业务范畴
     */
    private String businessScope;

    /**
     * 用车时间
     */
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

    @TableField(exist = false)
    private String departureStationName;

    /**
     * 目的城市
     */
    private String arrivalStation;

    @TableField(exist = false)
    private String arrivalStationName;

    /**
     * 始发地址
     */
    private String departureAddress;

    /**
     * 目的地址
     */
    private String arrivalAddress;

    /**
     * 订单备注
     */
    private String orderRemark;

    /**
     * 车辆ID
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer truckId;

    @TableField(exist = false)
    private String truckNumber;

    /**
     * 司机姓名
     */
    private String driverName;

    /**
     * 司机电话
     */
    private String driverTel;

    /**
     * 运费成本：单价
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal costUnitprice;

    /**
     * 运费成本：总价
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal costAmount;

    @TableField(exist = false)
    private BigDecimal costPriceAmount;
    @TableField(exist = false)
    private String costPriceType;
    /**
     * 运费成本：币种
     */
    private String costCurrecnyCode;

    /**
     * 成本分摊标准类型：订单、毛重、计重、体积
     */
    private String costShareMethod;

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
    private List<VlOrderDetailOrder> detailOrderList;


    /**
     * 吨位
     */
    @TableField(exist = false)
    private BigDecimal ton;

    @TableField(exist = false)
    private String tonStr;

    /**
     * 重量
     */
    @TableField(exist = false)
    private BigDecimal weight;

    @TableField(exist = false)
    private String weightStr;

    /**
     * 限重
     */
    @TableField(exist = false)
    private BigDecimal weightLimit;

    @TableField(exist = false)
    private String weightLimitStr;

    /**
     * 体积
     */
    @TableField(exist = false)
    private BigDecimal volume;

    @TableField(exist = false)
    private String volumeStr;

    /**
     * 最大体积
     */
    @TableField(exist = false)
    private BigDecimal volumeLimit;

    @TableField(exist = false)
    private String volumeLimitStr;

    /**
     * 车辆类型
     */
    @TableField(exist = false)
    private String truckType;

    @TableField(exist = false)
    private String awbNumber;
    @TableField(exist = false)
    private String customerNumber;

    @TableField(exist = false)
    private String columnStrs;

}
