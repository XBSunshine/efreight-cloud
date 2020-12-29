package com.efreight.afbase.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CssReportNoWriteoffDetail {
    private String businessScope;
    private String awbNumber;
    private String customerNumber;
    private String orderCode;
    private String orderId;
    private LocalDate flightDate;
    private String orderCoopCode;
    private String orderCoopName;
    private String coopCode;
    private String coopName;
    private String servicerName;
    private String salesName;
    private String currency;
    private BigDecimal amount;
    private String amountStr;
    private BigDecimal functionalAmount;
    private String functionalAmountStr;
    private BigDecimal functionalAmountWriteoff;
    private String functionalAmountWriteoffStr;
    private BigDecimal amountNoWriteoff;
    private String amountNoWriteoffStr;
    private BigDecimal functionalAmountNoWriteoff;
    private String functionalAmountNoWriteoffStr;
}
