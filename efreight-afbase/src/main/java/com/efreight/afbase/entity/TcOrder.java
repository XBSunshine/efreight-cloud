package com.efreight.afbase.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;

import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.util.List;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * TC 订单管理 TE、TI 订单
 * </p>
 *
 * @author caiwd
 * @since 2020-07-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tc_order")
public class TcOrder implements Serializable {

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
    @TableField(strategy = FieldStrategy.IGNORED)
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
     * 运单号码 Railway waybill number
     */
    private String rwbNumber;

    /**
     * 客户单号
     */
    private String customerNumber;

    /**
     * 客户ID
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer coopId;

    /**
     * 业务范畴
     */
    private String businessScope;

    /**
     * 铁路产品ID
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer railwayProductId;

    /**
     * 订舱代理ID
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer bookingAgentId;

    /**
     * 产品类型
     */
    private String productType;

    /**
     * 运输条款
     */
    private String transitClause;

    /**
     * 运费条款
     */
    private String paymentMethod;

    /**
     * 装箱方式
     */
    private String containerMethod;

    /**
     * 超标货物
     */
    private String overGoods;

    /**
     * 发车日期
     */
    private LocalDate expectDeparture;

    /**
     * 到达日期
     */
    private LocalDate expectArrival;

    /**
     * 起运地
     */
    private String departureStation;

    /**
     * 目的地
     */
    private String arrivalStation;

    /**
     * 中转低
     */
    private String transitStation;

    /**
     * 付款地
     */
    private String placePayment;

    /**
     * 出境口岸
     */
    private String exitPort;

    /**
     * 品名_中文
     */
    private String goodsNameCn;

    /**
     * 品名_英文
     */
    private String goodsNameEn;

    /**
     * 货物类型
     */
    private String goodsType;

    /**
     * 唛头
     */
    private String shippingMarks;

    /**
     * 鉴定情况
     */
    private String appraisalNote;

    /**
     * 包装类型
     */
    private String packageType;

    /**
     * 订单备注
     */
    private String orderRemark;

    /**
     * 集装箱量
     */
    private String containerList;

    /**
     * 标箱数量
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer containerNumber;

    /**
     * 件数
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer planPieces;

    /**
     * 毛重
     */
    private BigDecimal planWeight;

    /**
     * 体积
     */
    private BigDecimal planVolume;

    /**
     * 计费吨
     */
    private BigDecimal planChargeWeight;

    /**
     * 责任客服ID
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer servicerId;

    /**
     * 责任客服名称
     */
    private String servicerName;

    /**
     * 责任销售ID
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer salesId;

    /**
     * 责任销售名称
     */
    private String salesName;

    /**
     * 通知人信息
     */
    private String notifierRemark;

    /**
     * 目的港代理
     */
    private String arrivalAgent;

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
     * 运费卖价：备注
     */
    private String freightProfitRatioRemark;

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
     * 运费成本：备注
     */
    private String msrProfitRatioRemark;

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
     * 报关服务：1是 0否
     */
    private Boolean customsClearanceService;

    /**
     * 报关服务：报关代理
     */
    private String customsClearanceCompany;

    /**
     * 报关服务：查验日期
     */
    private LocalDate customsInspectionDate;

    /**
     * 报关服务：放行日期
     */
    private LocalDate customsClearanceDate;

    /**
     * 报关服务：报关备注
     */
    private String customsDeclaresRemark;

    /**
     * 报关服务：查验备注
     */
    private String customsInspectionRemark;

    /**
     * 申请放箱：1 是 0 否
     */
    private Boolean containerApply;

    /**
     * 申请放箱：放箱公司
     */
    private String containerApplyCompany;

    /**
     * 申请放箱：堆场信息
     */
    private String containerWarehouse;

    /**
     * 申请放箱：放箱令号
     */
    private String containerCommand;

    /**
     * 申请放箱：备注
     */
    private String containerApplyRemark;

    /**
     * 提柜服务：1是 0否
     */
    private Boolean containerPickupService;

    /**
     * 提柜服务：拖车公司
     */
    private String trailersCompany;

    /**
     * 提柜服务：车辆司机
     */
    private String trailersDriver;

    /**
     * 提柜服务：提柜日期
     */
    private LocalDate containerPickupDate;

    /**
     * 提柜服务：提柜地址
     */
    private String containerPickupAddress;

    /**
     * 提柜服务：送柜日期
     */
    private LocalDate containerArrivalDate;

    /**
     * 提柜服务：送柜地址
     */
    private String containerArrivalAddress;

    /**
     * 提柜服务：提柜备注
     */
    private String containerPickupRemark;

    /**
     * 装箱服务：1是 0否
     */
    private Boolean containerLoadService;

    /**
     * 装箱服务：装箱代理
     */
    private String containerLoadCompany;

    /**
     * 装箱服务：装箱日期
     */
    private LocalDate containerLoadDate;

    /**
     * 装箱服务：仓库名称
     */
    private String containerLoadWarehouse;

    /**
     * 装箱服务：仓库地址
     */
    private String containerLoadAddressCn;

    /**
     * 装箱服务：装箱备注
     */
    private String containerLoadRemark;

    /**
     * 目的港清关服务：1是 0否
     */
    private Boolean arrivalCustomsClearanceService;

    /**
     * 目的港清关服务：报关代理
     */
    private String arrivalCustomsClearanceCompany;

    /**
     * 目的港清关服务：查验日期
     */
    private LocalDate arrivalCustomsInspectionDate;

    /**
     * 目的港清关服务：放行日期
     */
    private LocalDate arrivalCustomsClearanceDate;

    /**
     * 目的港清关服务：报关备注
     */
    private String arrivalCustomsDeclaresRemark;

    /**
     * 目的港清关服务：查验备注
     */
    private String arrivalCustomsInspectionRemark;

    /**
     * 派送服务：1 是 0 否 目的港派送
     */
    private Boolean deliveryService;

    /**
     * 派送服务：卡车公司
     */
    private String deliveryCompany;

    /**
     * 派送服务：车辆司机
     */
    private String deliveryDriver;

    /**
     * 派送服务：收货人信息
     */
    private String deliverySigner;

    /**
     * 派送服务：送货地址
     */
    private String deliveryAddress;

    /**
     * 派送服务：送货日期
     */
    private LocalDate deliveryDate;

    /**
     * 派送服务：签收日期
     */
    private LocalDate deliverySignDate;

    /**
     * 派送服务：派送备注
     */
    private String deliveryRemark;

    /**
     * rowid
     */
    private String rowUuid;
    
    @TableField(exist = false)
    private String coopName;
    @TableField(exist = false)
    private String customerName;

    @TableField(exist = false)
    private BigDecimal msrPrice;
    @TableField(exist = false)
    private String msrType;
    @TableField(exist = false)
    private BigDecimal freightPrice;
    @TableField(exist = false)
    private String freightType;
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer containerLoadWarehouseId;
    @TableField(exist = false)
    private LocalDate expectDepartureStart;
    @TableField(exist = false)
    private LocalDate expectDepartureEnd;
    @TableField(exist = false)
    private LocalDate creatTimeStart;
    @TableField(exist = false)
    private LocalDate creatTimeEnd;
    @TableField(exist = false)
    private String bookingAgentName;
    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String planWeightStr;
    @TableField(exist = false)
    private String planChargeWeightStr;
    @TableField(exist = false)
    private String planVolumeStr;
    
    
}
