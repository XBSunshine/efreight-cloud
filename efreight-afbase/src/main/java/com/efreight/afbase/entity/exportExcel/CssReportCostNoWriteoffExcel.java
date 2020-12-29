package com.efreight.afbase.entity.exportExcel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CssReportCostNoWriteoffExcel {
    private String businessScope;
    private String awbNumber;
    private String orderCode;
    private String customerNumber;
    private LocalDate flightDate;
    private String orderCoopCode;
    private String orderCoopName;
    private String coopCode;
    private String coopName;
    private String servicerName;
    private String salesName;
    private String currency;
    private String amountStr;
    private String functionalAmountStr;
    private String amountNoWriteoffStr;
    private String functionalAmountNoWriteoffStr;
}
