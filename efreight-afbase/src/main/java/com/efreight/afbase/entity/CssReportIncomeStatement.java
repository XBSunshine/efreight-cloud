package com.efreight.afbase.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CssReportIncomeStatement {

    private LocalDate start;
    private LocalDate end;
    private Boolean voucherDateChecked;
    private Boolean lockDateChecked;
    private Integer orgId;
    private Integer otherOrg;
}
