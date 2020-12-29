package com.efreight.afbase.entity;

import java.math.BigDecimal;
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
    private LocalDateTime drivingTime;

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
    private Integer planPieces;

    /**
     * 预报毛重
     */
    private BigDecimal planWeight;

    /**
     * 预报体积
     */
    private BigDecimal planVolume;

    /**
     * 预报计重
     */
    private BigDecimal planChargeWeight;

    /**
     * 预报尺寸
     */
    private String planDimensions;

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

    /**
     * 实际体积
     */
    private BigDecimal confirmVolume;

    /**
     * 实际计重
     */
    private BigDecimal confirmChargeWeight;

    /**
     * 实际密度
     */
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
    private BigDecimal freightUnitprice;

    /**
     * 运费卖价：总价
     */
    private BigDecimal freightAmount;

    /**
     * 运费卖价：币种
     */
    private String freightCurrecnyCode;

    /**
     * 运费成本：单价
     */
    private BigDecimal msrUnitprice;

    /**
     * 运费成本：总价
     */
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


}
