package com.efreight.afbase.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class StatementPrint {

    //图片
    private String orgSeal;
    private String orgLogo;

    //公用内容
    private String currencyAmount;
    private String functionalAmount;

    //表格内容
    private String serial;
    private String awbNumber;
    private String debitNoteNum;
    private LocalDate flightDate;
    private String flightInfo;
    private String customerNumber;
    private String chargeableWeight;

    //表头内容
    private String orgName;
    private String orgEname;
    private String customerName;
    private String customerEname;
    private String statementNum;
    private LocalDate statementDate;
    private String creatorName;
    private String creatorEname;
    private LocalDateTime createTime;
    private String createTimeStr;
    private String statementRemark;
    private String phoneNumber;
    private String coopAddress;
    private String functionalAmountConvertBig;
    private String bankInfo;
}
