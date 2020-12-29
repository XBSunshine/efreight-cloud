package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_order")
public class OperationPlan {
    private static final long serialVersionUID = 1L;

    @TableId(value = "order_id", type = IdType.AUTO)
    private Integer 	orderId;
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
     * 分单数量
     */
    private Integer hawbQuantity;

    /**
     * 分单id
     */
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
    @TableField(strategy=FieldStrategy.IGNORED)
    private String receiptDate;

    /**
     * 是否可拼
     */
    private Boolean isConsol;

    /**
     * 预计航班号
     */
    private String expectFlight;

    /**
     * 预计航班日期(ETD)
     */
    private LocalDate expectDeparture;

    /**
     * 预计到达日期(ETA)
     */
    @TableField(strategy=FieldStrategy.IGNORED)
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
     * 始发货栈
     */
    private Integer departureWarehouseId;

    
    /**
     * 始发库房
     */
    @TableField(strategy=FieldStrategy.IGNORED)
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
    private Integer planPieces;

    /**
     * 预报毛重
     */
    private BigDecimal planWeight;

    /**
     * 预报体积
     */
    private Double planVolume;

    /**
     * 预报计费重量
     */
    private Double planChargeWeight;

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
//    private String confirmDimensions;

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

    /**
     * 付款方式
     */
    private String paymentMethod;

    /**
     * 运费单价
     */
    private Double freightUnitprice;

    /**
     * 运费总价
     */
    private Double freightAmount;

    /**
     * msr单价（成本单价）
     */
    private Double msrUnitprice;

    /**
     * msr总价（成本总价）
     */
    private Double msrAmount;

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

    /**
     * 修改时间
     */
    private Date editTime;

    //
    private transient String createTimeBegin;
    private transient String createTimeEnd;
    private transient String flightDateBegin;
    private transient String flightDateEnd;
    private transient String receiptDateStart;
    private transient String receiptDateEnd;
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
    private transient String coopCode;
    /**
     * 操作备注
     */
    private String operationRemark;
    /**
     * 外场操作备注
     */
    private String outfieldRemark;
    @TableField(exist = false)
    private String inboundDateStart;
    @TableField(exist = false)
    private String inboundDateEnd;
    @TableField(exist = false)
    private String printScope;
    @TableField(exist = false)
    private String slIds;
    @TableField(exist = false)
    private String columnStrs;
    @TableField(exist = false)
    private String pageName;//标记是从AE订单页面进入还是操作计划页面进入
}
