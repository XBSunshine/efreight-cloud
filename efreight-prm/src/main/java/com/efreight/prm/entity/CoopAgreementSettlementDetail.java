package com.efreight.prm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CoopAgreementSettlementDetail {

    private Integer settlementId;
    private String settlementType;
    private Integer agreementId;
    private Integer coopId;
    private String settlementModName;
    private String settlementPeriod;
    private Double unitPrice;
    private Double receiveCharge;
    private Double minCharge;
    private Double maxCharge;
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    private Integer quantityConfirmDays;
    private Integer quantityConfirmId;
    private String quantityConfirmName;
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    private Integer billConfirmDays;
    private Integer billConfirmId;
    private String billConfirmName;
    private Integer billConfirmContacts1;
    private String billConfirmContactsName;
    private Integer needEmail;
    private Integer elInvoice;
    private Integer specialInvoice;
    private Integer creatorId;
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    private Date createTime;
    private Integer editorId;
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    private Date editTime;
    private Integer orgId;
    private Integer deptId;
    private String coopName;
    private String paymentMethod;
   // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String beginDate;
    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String endDate;
    private String settlementModBillMonth;
    private transient List<Integer> billConfirmContacts;
    private transient String id;
    private String settlementState;
    private String billTemplate;
    private Double unitPriceExcessive;
    private Double baseQuantity;
    private Integer headOfficeConfirmId;
    private Integer settlementId1;
    private Integer groupId;
    private String departureStation;
    private String arrivalStation;
    private Integer serviceId;
    private String headOfficeConfirmName;
    private String remark;
    private String arrivalDeparturType;
    private Integer aircraftClassification;
    private String paramText;
    private String itCode;
    private Integer reviewItNeed1;
    private Integer reviewIt;
    private Integer reviewFinance;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startChargeTime;
    private Integer isSendMailAuto;
}
