package com.efreight.afbase.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.List;

import jdk.nashorn.internal.ir.annotations.Ignore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * AF 询价单
 * </p>
 *
 * @author xiaobo
 * @since 2020-05-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_order_Inquiry")
public class OrderInquiry implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 询价订单ID
     */
    @TableId(value = "order_inquiry_id", type = IdType.AUTO)
    private Integer orderInquiryId;

    /**
     * 询价单UUID
     */
    private String orderInquiryOrderUuid;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 业务范畴
     */
    private String businessScope;

    /**
     * 订单ID
     */
    private Integer orderId;

    /**
     * 订单UUID
     */
    private String orderUuid;

    /**
     * 询价单号
     */
    private String orderInquiryCode;

    /**
     * 询价单状态：已创建 / 已收方案 / 已转订单 / 已关闭
     */
    private String orderInquiryStatus;

    /**
     * 始发港
     */
    private String departureStation;

    /**
     * 目的港
     */
    private String arrivalStation;

    /**
     * 航司偏好：逗号分隔
     */
    private String carrierCode;

    /**
     * 直飞类型：  直飞
     */
    private String directFlight;

    /**
     * 预计航班日期(ETD)
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate expectDeparture;

    /**
     * 航班时间要求
     */
    private String flightRemark;

    /**
     * 航班要求
     */
    @TableField(exist = false)
    private String flightClaim;

    /**
     * 件数
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer planPieces;

    /**
     * 包装类型
     */
    private String packageType;

    /**
     * 毛重
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal planWeight;

    /**
     * 超重类型：超重
     */
    private String overWeight;

    /**
     * 体积
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal planVolume;

    /**
     * 超尺类型：超尺
     */
    private String overSize;

    /**
     * 是否特货
     */
    @TableField(exist = false)
    private String specialGoods;

    /**
     * 尺寸
     */
    private String planDimensions;

    /**
     * 计费重量
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal planChargeWeight;

    /**
     * 密度
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal planDensity;

    /**
     * 货物类型
     */
    private String goodsType;

    /**
     * 电池类型
     */
    private String batteryType;

    /**
     * 中文品名
     */
    private String goodsNameCn;

    /**
     * 价格预期：总价
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal planAmount;

    /**
     * 价格预期：单价
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal planUnitprice;

    /**
     * 询价备注
     */
    private String inquiryRemark;

    /**
     * 询盘代理ID：逗号分隔
     */
    private String inquiryAgentIds;

    /**
     * 询盘代理名称
     */
    @TableField(exist = false)
    private String inquiryAgentNames;
    /**
     * 创建人ID
     */
    private Integer creatorId;

    /**
     * 创建人
     */
    private String creatorName;

    /**
     * 创建人时间
     */
    private LocalDateTime creatTime;

    private Integer editorId;

    private String editorName;

    private LocalDateTime editTime;
    @TableField(exist = false)
    private LocalDateTime editTimeBegin;
    @TableField(exist = false)
    private LocalDateTime editTimeEnd;

    private String rowUuid;

    /**
     * 报价方案
     */
    @TableField(exist = false)
    private String inquiryPlan;

    /**
     * 该询价单的报价单列表
     */
    @TableField(exist = false)
    private List<OrderInquiryQuotation> OrderInquiryQuotations;

    @TableField(exist = false)
    private String columnStrs;


}
