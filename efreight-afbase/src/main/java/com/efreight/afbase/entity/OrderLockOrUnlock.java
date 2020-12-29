package com.efreight.afbase.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class OrderLockOrUnlock {

    private Integer orderId;
    private String orderUuid;
    private String businessScope;
    private String orderCode;
    private String awbNumber;
    private String customerNumber;
    private Boolean incomeFinishStatus;
    private Boolean costFinishStatus;
    private Integer incomeFinishStatusForSort;
    private Integer costFinishStatusForSort;
    private Boolean orderLockStatus;
    private String orderStatus;
    private String incomeStatus;
    private String costStatus;
    private BigDecimal incomeAmount;
    private BigDecimal costAmount;
    private BigDecimal profitAmount;
    private String flightNo;
    private LocalDate lockDate;
    private LocalDate flightDate;
    private LocalDate flightDateStart;
    private LocalDate flightDateEnd;
    private String departureStation;
    private String arrivalStation;
    private String coopCode;
    private String coopName;
    private String awbFromName;
    private String salesName;
    private String servicerName;
    private String goodsType;
    private Integer planPieces;
    private BigDecimal planWeight;
    private BigDecimal planVolume;
    private BigDecimal planChargeWeight;
    private Integer orgId;
    private String columnStrs;
    private String businessMethod;
    private Integer signState;
}
