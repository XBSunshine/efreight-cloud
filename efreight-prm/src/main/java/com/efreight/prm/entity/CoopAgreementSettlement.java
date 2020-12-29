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
public class CoopAgreementSettlement {

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
    private String coopCode;
    private String itCode;//保存修改用
    private String paymentMethod;
   // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String beginDate;
    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String endDate;
    private String settlementModBillMonth;
    private transient List<Integer> billConfirmContacts;
    private transient List<CoopAgreementSettlementDetail> coopAgreementSettlementDetail;
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
    private String creatorName;
    private String editorName;
    private Integer serviceId;
    private String headOfficeConfirmName;
    private String settlementState1;
    private String remark;
    private Integer transactorId;
    private String userName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date overDate;
    private String arrivalDeparturType;
    private String aircraftClassification;
    private String settlementIdDetails;//用于在组上做审核和作废操作，保存组下面子集的Id
    private String haveChild;

    private String invoiceTitle;
    private String invoiceType;
    private String invoiceRemark;
    private transient List<Integer> invoiceReceiveEmails;

    private Integer salesCollaborativeId;
    private Integer regionalHeadId;
    private String isNeedVerify;
    private Boolean reviewItNeed;
    private Integer reviewItNeed1;
    private String isNeedVerifyIt;
    private Integer reviewIt;
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    private Date reviewFinanceTime;
    private String reviewFinanceName;
    private Integer reviewFinance;
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    private Date reviewItTime;
    private String reviewItName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startChargeTime;
}
