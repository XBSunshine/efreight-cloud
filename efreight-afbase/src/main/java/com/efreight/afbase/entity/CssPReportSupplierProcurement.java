package com.efreight.afbase.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CssPReportSupplierProcurement {

    private String businessScope;
    private Integer coopId;
    private String coopCode;
    private String coopName;
    private String coopType;
    private Integer orderCount;
    private BigDecimal orderCountRatio;
    private BigDecimal chargeWeight;
    private BigDecimal chargeWeightRatio;
    private BigDecimal costFunctionalAmount;
    private BigDecimal costFunctionalAmountRatio;
    private BigDecimal unitCostFunctionalAmount;
    private String orderCode;
    private Integer yearOrderCount;
    private BigDecimal yearChargeWeight;
    private BigDecimal yearCostFunctionalAmount;
    private LocalDate statisticalPeriodStart;
    private LocalDate statisticalPeriodEnd;
    private String containerMethod;
    private String supplierType;
    private String goodsType;
    private String statisticalPeriodType;
    private Boolean isLock;
    private Boolean isAll;
    private Integer orgId;
    private String columnStrs;
    private Integer otherOrg;
}
