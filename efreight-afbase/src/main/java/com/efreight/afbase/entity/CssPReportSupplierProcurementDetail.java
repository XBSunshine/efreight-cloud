package com.efreight.afbase.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CssPReportSupplierProcurementDetail {
    private String businessScope;
    private String goodsSourceCode;
    private String goodsType;
    private String awbFrom;
    private String routingName;
    private String coopCode;
    private String coopName;
    private Integer orderId;
    private String orderUuid;
    private String orderCode;
    private String awbNumber;
    private String customerNumber;
    private String businessProduct;
    private String salesName;
    private String servicerName;
    private String flightNo;
    private LocalDate flightDate;
    private String departureStation;
    private String arrivalStation;
    private BigDecimal chargeWeight;
    private BigDecimal incomeFunctionalAmount;
    private BigDecimal costFunctionalAmount;
    private BigDecimal grossProfit;
    private BigDecimal unitCostAmount;
    private BigDecimal unitGrossProfit;
    private BigDecimal grossProfitRatio;
    private BigDecimal functionalAmountWriteoff;
    private BigDecimal functionalAmountNoWriteoff;
    private LocalDate statisticalPeriodStart;
    private LocalDate statisticalPeriodEnd;
    private String containerMethod;
    private String statisticalPeriodType;
    private Integer supplierId;
    private Boolean isLock;
    private Integer orgId;
    private String mainRouting;
    private String mainRoutingIncome;
    private String mainRoutingCost;
    private String feeder;
    private String feederIncome;
    private String feederCost;
    private String operation;
    private String operationIncome;
    private String operationCost;
    private String packaging;
    private String packagingIncome;
    private String packagingCost;
    private String storage;
    private String storageIncome;
    private String storageCost;
    private String postage;
    private String postageIncome;
    private String postageCost;
    private String clearance;
    private String clearanceIncome;
    private String clearanceCost;
    private String exchange;
    private String exchangeIncome;
    private String exchangeCost;
    private String columnStrs;
    private Boolean showConstituteFlag;
    private Integer otherOrg;

}
