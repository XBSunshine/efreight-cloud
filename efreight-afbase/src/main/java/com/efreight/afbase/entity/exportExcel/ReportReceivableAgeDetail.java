package com.efreight.afbase.entity.exportExcel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ReportReceivableAgeDetail implements Serializable {

    private String businessScope;
    private String awbNumber;
    private String orderCode;
    private String orderId;
    private String orderCoopCode;
    private String orderCoopName;
    private String coopCode;
    private String coopName;
    private String servicerName;
    private String salesName;
    private LocalDate ETD;
    private String functionalAmount;
    private String functionalAmountWriteoff;
    private String noFunctionalAmountWriteoff;

}
