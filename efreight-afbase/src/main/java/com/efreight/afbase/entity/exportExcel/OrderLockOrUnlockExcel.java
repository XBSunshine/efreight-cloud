package com.efreight.afbase.entity.exportExcel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OrderLockOrUnlockExcel {
    private String orderCode;
    private String awbNumber;
    private String customerNumber;
    private String incomeFinishStatus;
    private String costFinishStatus;
    private String orderLockStatus;
    private String orderStatus;
    private String incomeAmount;
    private String costAmount;
    private String profitAmount;
    private LocalDate lockDate;
    private String flightNo;
    private LocalDate flightDate;
    private String businessMethod;
    private String departureStation;
    private String arrivalStation;
    private String coopCode;
    private String coopName;
    private String awbFromName;
    private String salesName;
    private String servicerName;
    private String goodsType;
    private Integer planPieces;
    private String planWeight;
    private String planVolume;
    private String planChargeWeight;
}
