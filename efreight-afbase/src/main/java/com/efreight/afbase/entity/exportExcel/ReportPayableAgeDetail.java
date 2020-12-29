package com.efreight.afbase.entity.exportExcel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ReportPayableAgeDetail implements Serializable {

    private String businessScope;
    private String awbNumber;
    private LocalDate ETD;
    private String orderCode;
    private String orderCoopCode;
    private String orderId;
    private String orderCoopName;
    private String coopCode;
    private String coopName;
    private String servicerName;
    private String salesName;
    private String currency;
    private String amount;
    private String functionalAmount;
    private String noAmountWriteoff;
    private String noFunctionalAmountWriteoff;
}
