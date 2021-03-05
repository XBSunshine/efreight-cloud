package com.efreight.afbase.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sc_order")
public class ScOrder implements Serializable {

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
     * 成本状态
     */
    private String costStatus;

    /**
     * 主提单号
     */
    private String mblNumber;

    /**
     * 分提单号
     */
    private String hblNumber;

    /**
     * 客户单号/合约号
     */
    private String customerNumber;


    /**
     * 客商资料ID
     */
    private Integer coopId;

    /**
     * 业务范畴
     */
    private String businessScope;

    /**
     * 运输条款
     */
    private String transitClause;

    /**
     * 放货方式
     */
    private String dischargingMethod;

    /**
     * 装箱方式
     */
    private String containerMethod;

    /**
     * 运费条款
     */
    private String paymentMethod;

    /**
     * 船名
     */
    private String shipName;

    /**
     * 船次号
     */
    private String shipVoyageNumber;

    /**
     * 预计出发日期
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate expectDeparture;

    /**
     * 预计到达日期
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate expectArrival;

    /**
     * 始发港
     */
    private String departureStation;

    /**
     * 目的港
     */
    private String arrivalStation;

    /**
     * 中转港
     */
    private String transitStation;

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
    private Integer containerNumber;

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
     * 预报计费吨
     */
    private BigDecimal planChargeWeight;

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
     * 换单服务：1是 0否
     */
    private Boolean changeOrderService;
    /**
     * 换单日期
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate changeOrderDate;

    /**
     * 换单备注
     */
    private String changeOrderRemark;

    /**
     * 换单代理
     */
    private String changeOrderAgent;

    /**
     * 库内服务：1是 0否
     */
    private Boolean warehouseService;

    /**
     * 库内服务：操作公司
     */
    private String warehouseOperator;

    /**
     * 库内服务： 入库日期
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate inboundDate;

    /**
     * 库内服务：出库日期
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate outboundDate;

    /**
     * 库内服务：破损记录
     */
    private String damageRemark;

    /**
     * 库内服务：操作备注
     */
    private String operationRemark;

    /**
     * 报关服务/清关服务：1是 0否
     */
    private Boolean customsClearanceService;

    /**
     * 报关服务/清关服务：报关代理
     */
    private String customsClearanceCompany;

    /**
     * 报关服务/清关服务：查验日期
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate customsInspectionDate;

    /**
     * 报关服务/清关服务：放行日期
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate customsClearanceDate;

    /**
     * 报关服务/清关服务：报关备注
     */
    private String customsDeclaresRemark;

    /**
     * 报关服务/清关服务：查验备注
     */
    private String customsInspectionRemark;

    /**
     * 派送服务：1 是 0 否
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
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate deliveryDate;

    /**
     * 派送服务：签收日期
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate deliverySignDate;

    /**
     * 派送服务：派送备注
     */
    private String deliveryRemark;

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
     * 通知人
     */
    private String notifierRemark;

    /**
     * 起运港代理
     */

    private String departureStationAgent;
    /**
     * 船公司
     */
    private Integer carrierId;

    /**
     * 订舱代理
     */
    private Integer bookingAgentId;

    /**
     * 付款地
     */
    private String placePayment;

    /**
     * 提单类型
     */
    private String billingType;

    /**
     * 出单类型
     */
    private String billingMethod;

    /**
     * 截关日期
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate customsClosingDate;

    /**
     * 截单日期
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate documentOffDate;

    /**
     * 正本份数
     */
    private Integer originalsNumber;

    /**
     * 副本份数
     */
    private Integer copyNumber;

    /**
     * 签发日期
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate issueDate;

    /**
     * 目的港代理
     */
    private String arrivalAgent;

    /**
     * 货物描述
     */
    private String goodsRemark;

    /**
     * 唛头
     */
    private String shippingMarks;

    /**
     * 提柜服务
     */
    private Boolean containerPickupService;
    /**
     * 拖车公司
     */
    private String trailersCompany;
    /**
     * 车辆司机
     */
    private String trailersDriver;
    /**
     * 提柜日期
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate containerPickupDate;
    /**
     * 提柜地址
     */
    private String containerPickupAddress;
    /**
     * 送柜日期
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate containerArrivalDate;
    /**
     * 送柜地址
     */
    private String containerArrivalAddress;
    /**
     * 提柜备注
     */
    private String containerPickupRemark;

    /**
     * 装箱服务
     */
    private Boolean containerLoadService;
    /**
     * 装箱代理
     */
    private String containerLoadCompany;
    /**
     * 装箱日期
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate containerLoadDate;
    /**
     * 仓库名称
     */
    private String containerLoadWarehouse;
    /**
     * 仓库地址
     */
    private String containerLoadAddressCn;
    /**
     * 装箱备注
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
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate arrivalCustomsInspectionDate;
    /**
     * 目的港清关服务：放行日期
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate arrivalCustomsClearanceDate;
    /**
     * 目的港清关服务：报关备注
     */
    private String arrivalCustomsDeclaresRemark;
    /**
     * 目的港清关服务：查验备注
     */
    private String arrivalCustomsInspectionRemark;
    
    private Boolean incomeRecorded;
    private Boolean costRecorded;

    private String rowUuid;
}
