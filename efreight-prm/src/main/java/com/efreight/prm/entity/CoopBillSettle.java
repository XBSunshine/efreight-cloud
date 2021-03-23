package com.efreight.prm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CoopBillSettle implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer billNumber;
    private Integer statementId;
    private String statementDate;
    private String coopName;
    private String statementName;
    private String statementStatus;
    private BigDecimal amountReceived;
    private String billTemplate;
    private String saleConfirmName;
    private String billConfirmName;
    private String serviceNameOne;
    private String serviceNameTwo;
    private String settlementType;
    private String settlementPeriod;
    private String paymentMethod;
    private Double unitPrice;
    private Integer fillNumber;
    private BigDecimal acturalCharge;
    private String departureStation;
    private String beginDate;
    private String endDate;
    private Integer orgId;
    private String statementDate_begin;
    private String statementDate_end;
    private String validBeginDate;
    private String validEndDate;
    private BigDecimal acturalChargeTotal;
    private BigDecimal amountReceivedTotal;
    private String billStatus;
    private String periodAndMethod;
    private String itCode;
    private String invoiceWriteoffDate;
    private String invoiceWriteoffUserName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date confirmCustomerTime;
    private String invoiceWriteoffDateBegin;
    private String invoiceWriteoffDateEnd;
    private String salesCollaborativeName;
    private String startChargeTime;
    private Boolean isNewBusiness;
    private Boolean showZeroFlag;
    private String columnStrs;
}
