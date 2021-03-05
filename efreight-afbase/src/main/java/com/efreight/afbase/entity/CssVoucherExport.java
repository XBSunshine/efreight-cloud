package com.efreight.afbase.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CssVoucherExport {

    private Integer invoiceDetailId;
    private Integer invoiceDetailWriteoffId;
    private String businessScope;
    private Boolean voucherStatus;
    private String voucherNumber;
    private LocalDate voucherDate;
    private LocalDate voucherDateStart;
    private LocalDate voucherDateEnd;
    private String voucherCreatorName;
    private Integer voucherCreatorId;
    private Integer customerId;
    private String customerName;
    private BigDecimal amount;
    private String amountStr;
    private Integer orgId;
    private Integer voucherIsDetail;

    private LocalDate writeoffDate;
    private LocalDate writeoffDateStart;
    private LocalDate writeoffDateEnd;
    private LocalDate invoiceDate;
    private LocalDate invoiceDateStart;
    private LocalDate invoiceDateEnd;
    private String invoiceNum;
    private String invoiceType;
    private String invoiceTitle;
    private BigDecimal writeoffAmount;
    private String currency;
    private String writeoffAmountStr;
    private String writeoffNum;
    private String writeoffCreatorName;
    private Integer writeoffId;
    private String writeoffType;
    private String financialAccountCode;
    private String financialAccountName;

    private Integer expenseReportId;
    private String expenseReportStatus;
    private LocalDate expenseReportDate;
    private LocalDate expenseReportDateStart;
    private LocalDate expenseReportDateEnd;
    private String expenseReportNum;
    private String expensesUse;
    private String expenseCreatorName;
    private String approvalFinancialUserName;
    private String paymentMethod;
    private String expenseReportMode;
    private BigDecimal expenseAmount;
    private String expenseAmountStr;
    private String expenseReportRemark;
    private String expenseFinancialAccountCode;
    private String expenseFinancialAccountName;
    private String bankFinancialAccountCode;
    private String bankFinancialAccountName;

    private String financialAccount;
    private String bankFinancialAccount;

    private String sql;
    private List<CssVoucherExport> checkedList;

    /**
     * 类型 0-收入挂账；1-成本挂账；2-收入核销；3-成本核销
     */
    private Integer type;
    private String columnStrs;

    private String rowUuid;

    private Integer paymentId;
}
