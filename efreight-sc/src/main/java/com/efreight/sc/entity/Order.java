package com.efreight.sc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * CS 订单管理 SI订单
 * </p>
 *
 * @author xiaobo
 * @since 2020-03-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sc_order")
public class Order implements Serializable {

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
     * 客户名称
     */
    @TableField(exist = false)
    private String customerName;

    /**
     * 客商资料ID
     */
    @TableField(strategy = FieldStrategy.IGNORED)
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
     * 应收状态
     */
    private String incomeStatus;

    /**
     * 成本状态
     */
    private String costStatus;
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
    @TableField(exist = false)
    private LocalDate expectDepartureStart;
    @TableField(exist = false)
    private LocalDate expectDepartureEnd;

    /**
     * 预计到达日期
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate expectArrival;
    @TableField(exist = false)
    private LocalDate expectArrivalStart;
    @TableField(exist = false)
    private LocalDate expectArrivalEnd;

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
     * 集装箱量明细
     */
    @TableField(exist = false)
    private List<OrderContainerDetails> containerDetails;

    /**
     * 标箱数量
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer containerNumber;

    /**
     * 预报件数
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer planPieces;

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
     * 预报计费吨
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal planChargeWeight;
    @TableField(exist = false)
    private String planChargeWeightStr;

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
    @TableField(exist = false)
    private LocalDateTime createTimeBegin;
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
     * 通知人
     */
    private String notifierRemark;

    /**
     * 起运港代理
     */

    private String departureStationAgent;
    /**
     * 收货人
     */
    @TableField(exist = false)
    private OrderShipperConsignee consignee;
    @TableField(exist = false)
    private OrderShipperConsignee shipper;

    /**
     * 船公司
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer carrierId;
    @TableField(exist = false)
    private String carrierName;

    /**
     * 订舱代理
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer bookingAgentId;
    @TableField(exist = false)
    private String bookingAgentName;

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

    @TableField(exist = false)
    private LocalDate customsClosingDateStart;
    @TableField(exist = false)
    private LocalDate customsClosingDateEnd;

    /**
     * 截单日期
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate documentOffDate;

    @TableField(exist = false)
    private LocalDate documentOffDateStart;
    @TableField(exist = false)
    private LocalDate documentOffDateEnd;

    /**
     * 正本份数
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer originalsNumber;

    /**
     * 副本份数
     */
    @TableField(strategy = FieldStrategy.IGNORED)
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
     * 还柜日期
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate containerReturnDate;
    /**
     * 还柜地址
     */
    private String containerReturnAddress;
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
     * 制单备注
     */
    private String waybillRemark;

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

    @TableField(exist = false)
    private String columnStrs;
    private Boolean incomeRecorded;
    private Boolean costRecorded;
    @TableField(exist = false)
    private Integer incomeRecordedForSort;
    @TableField(exist = false)
    private Integer costRecordedForSort;

    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer containerLoadWarehouseId;
    @TableField(exist = false)
    private String contactNameSe;

    /**
     * 箱号+铅封号
     */
    @TableField(exist = false)
    private String containerNumberAndContainerSealNo;
    @TableField(exist = false)
    private Integer amountFlag;
    @TableField(exist = false)
    private Integer amountFlagOrderId;
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

    /**
     * 订仓编号
     */
    private String bookingNumber;

    /**
     * 起运港英文名称
     */
    @TableField(exist = false)
    private String departureStationNameEn;

    /**
     * 目的港英文名称
     */
    @TableField(exist = false)
    private String arrivalStationNameEn;

    /**
     * 中转港英文名称
     */
    @TableField(exist = false)
    private String transitStationNameEn;

    /**
     * 起运港中文名称
     */
    @TableField(exist = false)
    private String departureStationNameCn;

    /**
     * 目的港中文名称
     */
    @TableField(exist = false)
    private String arrivalStationNameCn;

    /**
     * 中转港中文名称
     */
    @TableField(exist = false)
    private String transitStationNameCn;
}
