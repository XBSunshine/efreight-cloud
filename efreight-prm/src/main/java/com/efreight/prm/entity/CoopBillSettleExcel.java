package com.efreight.prm.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CoopBillSettleExcel {
    private String billNumber;
    private String statementDate;
    private String coopName;
    private String statementName;
    private String serviceNameOne;
    private String serviceNameTwo;
    private String departureStation;
    private String periodAndMethod;
    private String fillNumber;
    private Double unitPrice;
    private String acturalCharge1;
    private String billTemplate;
    private String statementStatus;
    private String saleConfirmName;
    private String billConfirmName;
    private String salesCollaborativeName;
    private Date confirmCustomerTime;
    private String invoiceWriteoffUserName;
    private String invoiceWriteoffDate;
    private String startChargeTime;
    private String validBeginDate;
    private String validEndDate;
    private String itCode;
}
