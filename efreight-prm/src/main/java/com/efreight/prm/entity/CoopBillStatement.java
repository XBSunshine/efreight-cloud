package com.efreight.prm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CoopBillStatement implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer statement_id;
    private Integer orgId;
    private Integer coopId;
    private String statementStatus;
    private String statementNumber;
    private String billName;//账单月份
    private Double acturalCharge;//应收金额
    private Double invoiceAmount;//折后金额
    private Double discount;//折扣
    private Integer creatorId;
    private String creatorName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    private String billIds;
    private String coopName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date statementMailDate;
    private String invoiceNumber;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTimeStart;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTimeEnd;
    private String editorName;
    private Integer editorId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date editTime;
    private String billConfirmName;
    private Integer statementMailSenderId;
    private String statementMailSenderName;
    private String statementName;
    private String invoiceUserName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date invoiceDate;
    private String invoiceWriteoffUserName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date invoiceWriteoffDate;
    private String billTemplate;

    private Integer settlementId;
    private String invoiceTitle;
    private String invoiceType;
    private String invoiceRemark;
    private String invoiceMailTo;
    private String invoiceReceiveEmail;
    private transient List<Integer> invoiceReceiveEmails;
    private String expressCompany;
    private String expressNumber;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date confirmCustomerTime;
    private String confirmCustomerName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date confirmSalerTime;
    private String confirmSalerName;

    private String confirmCustomerTime_begin;
    private String confirmCustomerTime_end;

    private BigDecimal invoiceAmountTotal;//账单金额合计

    private String invoiceWriteoffDate_begin;
    private String invoiceWriteoffDate_end;
    private String invoiceDate_begin;
    private String invoiceDate_end;
    private String isNeedUpdateInvoiceDate;
    private String billManualMailTo;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date mailSendTime;
}
