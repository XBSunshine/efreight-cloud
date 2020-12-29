package com.efreight.prm.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CoopBillMadeExcel {
    private String statementName;
    private String statementStatus;
    private String billName;
    private String invoiceAmount;
    private String billTemplate;
    private String billConfirmName;
    private String confirmCustomerTime;
    private String invoiceTitle;
    private String invoiceType;
    private String invoiceNumber;
    private String invoiceUserName;
    private String invoiceDate;
    private String invoiceWriteoffUserName;
    private String invoiceWriteoffDate;
    private String expressNumber;
    private String sendBillFlag;
    private String invoiceReceiveEmail;
}
