package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_order")
public class AfOrder implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 是否主单货
     */
    private Boolean isMwb;

    /**
     * 主单id
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer awbId;

    /**
     * 主单uuid
     */
    private String awbUuid;

    /**
     * 主单号
     */
    private String awbNumber;
    /**
     * 运单来源ID
     */
    private transient Integer awbFromId;
    /**
     * 运单来源名称
     */
    private transient String awbFromName;

    /**
     * 运单来源代码
     */
    private transient String supplierCode;
    /**
     * 分单数量
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer hawbQuantity;

    /**
     * 分单id
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer hawbId;

    /**
     * 分单号
     */
    private String hawbNumber;

    /**
     * 客户单号
     */
    private String customerNumber;

    /**
     * 客户代码
     */
    private transient String customerCode;

    /**
     * 客户项目id
     */
    private Integer projectId;
    /**
     * 客户id
     */
    private Integer coopId;
    private Integer servicerId;
    private String servicerName;
    private Integer salesId;
    private String salesName;

    /**
     * 业务范畴
     */
    private String businessScope;

    /**
     * 服务产品
     */
    private String businessProduct;

    /**
     * 运输条款
     */
    private String transitClause;

    /**
     * 到货方式
     */
    private String arrivalMethod;

    /**
     * 到货日期
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate receiptDate;

    /**
     * 是否可拼
     */
    private Boolean isConsol;

    /**
     * 是否非木
     */
    private Boolean isNoSolidWood;

    /**
     * 预计航班号
     */
    private String expectFlight;

    /**
     * 预计航班日期(ETD)
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate expectDeparture;

    /**
     * 预计到达日期(ETA)
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
     * 目的港代理
     */
    private String arrivalAgent;

    /**
     * 中转港
     */
    private String transitStation;

    /**
     * 中转港
     */
    @TableField("transit_station_2")
    private String transitStation2;


    /**
     * 始发货栈
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer departureWarehouseId;


    /**
     * 始发库房
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer departureStorehouseId;


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
     * 危险品类型
     */
    private String dangerousType;
    private String appraisalCompany;

    /**
     * 电池类型
     */
    private String batteryType;

    /**
     * 包装类型
     */
    private String packageType;

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

    /**
     * 预报体积
     */
    @TableField(strategy = FieldStrategy.IGNORED)
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

    /**
     * 实际尺寸
     */
    private String confirmDimensions;

    /**
     * 入库照片
     */
//    private String inboundFileUrl;

    /**
     * 结算计费重量
     */
    private Double settleChargeWeight;

    /**
     * 运费币种
     */
    private String currecnyCode;

    //成本币种

    private String msrCurrecnyCode;

    /**
     * 付款方式
     */
    private String paymentMethod;

    /**
     * 运费单价
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Double freightUnitprice;

    /**
     * 运费总价
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Double freightAmount;

    /**
     * 客户分泡
     */
    private String freightProfitRatioRemark;
    /**
     * msr单价（成本单价）
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Double msrUnitprice;

    /**
     * msr总价（成本总价）
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Double msrAmount;

    /**
     * 随机文件
     */
    private String airborneDocument;

    /**
     * 价格备注
     */
    private String priceRemark;
    /**
     * 成本分泡
     */
    private String msrProfitRatioRemark;
    /**
     * 主单付费重量
     */
    private Double awbCostChargeWeight;
    /**
     * 主单成本单价
     */
    private Double awbMsrUnitprice;
    /**
     * 主单成本单价
     */
    private Double awbMsrAmount;
    /**
     * 订单收费重量
     */
    private Double incomeChargeWeight;

    /**
     * 销售费用单价
     */
    private Double sellingCostUnitprice;

    /**
     * 销售费用总价
     */
    private Double sellingCostAmount;

    /**
     * 利润分成deptid
     */
    private Integer shareProfitId;

    /**
     * 利润分成模式
     */
    private String shareProfitType;

    /**
     * 利润分成标准
     */
    private Double shareProfitNumber;

    /**
     * 创建人
     */
    private Integer creatorId;

    private String creatorName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改人
     */
    private Integer editorId;

    private String editorName;
    //鉴定情况
    private String appraisalNote;
    //备注
    private String orderRemark;
    //handling_info
    private String handlingInfo;
    //唛头
    private String shippingMarks;
    //海关代码
    private String customsStatusCode;
    //操作备注
    private String operationRemark;
    //货物流向
    private String cargoFlowType;
    //货物流向备注
    private String cargoFlowRemark;

    //通知人信息
    private String notifierRemark;
    //始发港代理
    private String departureAgent;

    //外库服务
    private Boolean switchAwbService;
    //外库服务_调单日期
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate switchAwbDate;
    //外库服务_调单地址
    private String switchAwbAddress;
    //外库服务_调单备注
    private String switchAwbRemark;

    //库内服务
    private Boolean warehouseService;
    //库内服务_操作公司
    private String warehouseOperator;
    //库内服务_入库日期
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate inboundDate;
    //库内服务_出库日期
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate outboundDate;
    //库内服务_破损记录
    private String damageRemark;

    //报关服务
    private Boolean customsClearanceService;
    //报关类型
    private String customsDeclaresType;
    //报关代理
    private String customsClearanceCompany;
    //查验日期
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate customsInspectionDate;
    //放行日期
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate customsClearanceDate;
    //查验备注
    private String customsInspectionRemark;
    //报关备注
    private String customsDeclaresRemark;

    //派送服务
    private Boolean deliveryService;
    //卡车公司
    private String deliveryCompany;
    //车辆司机
    private String deliveryDriver;
    //收货人信息
    private String deliverySigner;
    //送货地址
    private String deliveryAddress;
    //送货日期
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate deliveryDate;
    //签收日期
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate deliverySignDate;
    //派送备注
    private String deliveryRemark;

    /**
     * 提货服务
     */
    private Boolean pickUpDeliveryService;

    /**
     * 提货公司
     */
    private String pickUpDeliveryCompany;
    /**
     * 提货地址
     */
    private String pickUpAddress;
    /**
     * 提货日期
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDate pickUpDeliveryDate;
    /**
     * 送货地址
     */
    private String pickUpDeliveryAddress;
    /**
     * 提货备注
     */
    private String pickUpDeliveryRemark;

    /**
     * 外场服务
     */
    private Boolean outfieldService;
    /**
     * 送货公司
     */
    private String outfieldDeliveryCompany;
    /**
     * 车牌号
     */
    private String outfieldTruckNumber;
    /**
     * 司机信息
     */
    private String outfieldDriver;
    /**
     * 打板代理
     */
    private String buildUpCompany;
    /**
     * 外场操作备注
     */
    private String outfieldRemark;

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


    //收发货人
    private transient AfOrderShipperConsignee afOrderShipperConsignee1;
    private transient AfOrderShipperConsignee afOrderShipperConsignee2;

    /**
     * 修改时间
     */
    private Date editTime;

    //
    private transient String createTimeBegin;
    private transient String createTimeEnd;
    private transient String flightDateBegin;
    private transient String flightDateEnd;
    /**
     * 始发库房名称
     */
    private transient String departureStorehouseName;
    /**
     * 始发货栈名称
     */
    private transient String departureWarehouseName;

    private transient String salesManagerName;

    private transient String projectName;
    private transient String coopName;
    private transient String contactName;
    private transient String priceType2;
    private transient Double priceValue2;
    private transient Double priceValue4;
    private transient BigDecimal incomeExchangeRate;
    private transient BigDecimal costExchangeRate;
    /**
     * 币种汇率
     */
    private transient BigDecimal currencyRate;
    private transient List<Integer> orderContacts;

    @TableField(exist = false)
    private List<AfShipperLetter> shipperLetters;


    /**
     * 出重信息
     */
    private transient BigDecimal inboundOrderChargeWeight;
    private transient BigDecimal inboundAwbChargeWeight;

    private String rowUuid;

    /**
     * 应收状态
     */
    private String incomeStatus;

    /**
     * 应付状态
     */
    private String costStatus;

    /**
     * 询价单id
     */
    private Integer orderInquiryId;

    /**
     * 报价方案id
     */
    @TableField(exist = false)
    private Integer orderInquiryQuotationId;

    @TableField(exist = false)
    private String orderInquiryRowUuid;

    @TableField(exist = false)
    private String columnStrs;

    private Boolean incomeRecorded;
    private Boolean costRecorded;

    @TableField(exist = false)
    private Integer incomeRecordedForSort;
    @TableField(exist = false)
    private Integer costRecordedForSort;

    /**
     * 杂费付款方式
     */
    private String paymentMethodOther;

    /**
     * 危险品编号
     */
    @TableField("UNDG_code")
    private String undgCode;

    /**
     * 危险品联系人
     */
    @TableField("UNDG_contact_name")
    private String undgContactName;

    /**
     * 危险品联系人通讯类型
     */
    @TableField("UNDG_contact_communication_type")
    private String undgContactCommunicationType;

    /**
     * 危险品联系人通讯号码
     */
    @TableField("UNDG_contact_communication_no")
    private String undgContactCommunicationNo;

    /**
     * 装载时间
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private LocalDateTime loadingDate;
    /**
     * 是否分批
     */
    private Boolean partialShipment;

    /**
     * 货源地
     */
    private String goodsSourceCode;
    /*
    主单舱单状态
     */
    private String manifestStatus;

    //分享源所在的签约公司
    @TableField(exist = false)
    private Integer orderShareOrgId;
    //分享源 所指定的orderId
    @TableField(exist = false)
    private Integer orderShareOrderId;
    //分享源所指定的客商ID
    @TableField(exist = false)
    private Integer OrderShareCoopId;
    //分享源所绑定的客商资料ID
    @TableField(exist = false)
    private Integer coopOrgCoopId;

    private transient String presets;
    /**
     * 审结
     */
    private transient String audit;
    private transient String arrived;
    private transient String passed;
    /**
     * 查验
     */
    private transient String checked;
    private transient String ams;
    private transient String entryPlate;

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
     * 航线
     */
    @TableField(exist = false)
    private String routingName;

    /**
     * 签单ID
     */
    @TableField(exist = false)
    private Integer rountingSignId;

    /**
     * 签单状态
     */
    @TableField(exist = false)
    private Integer signState;

    /**
     * 航线签单
     */
    @TableField(exist = false)
    private Integer rountingSign;

    /**
     * 航线签单-服务产品
     */
    @TableField(exist = false)
    private String rountingSignBusinessProduct;

    /**
     * 航线负责人
     */
    @TableField(exist = false)
    private String routingPersonName;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 托盘材质
     */
    @TableField(exist = false)
    private String palletMaterial;

    /**
     * 特货包装
     */
    @TableField(exist = false)
    private String specialPackage;

    /**
     * 温度要求
     */
    @TableField(exist = false)
    private String celsiusRequire;

    /**
     * 温度计
     */
    @TableField(exist = false)
    private Integer thermometer;

    /**
     * 是否有温度要求
     */
    @TableField(exist = false)
    private Boolean isCelsiusRequire;

}
