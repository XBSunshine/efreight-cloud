package com.efreight.afbase.entity.exportExcel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class IncomeExcel {

    private String businessScope;
    private String awbNumber;
    private String orderCode;
    private LocalDate flightDate;
    private String orderCustomerName;
    private String salesName;
    private String servicerName;
    private String customerName;
    private String serviceName;
    private String incomeAmountStr;
}
