package com.efreight.prm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CoopBill implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer billId;
    private Integer settlementId;
    private Integer agreementId;
    private String settlementType;
    private Double planCharge;
    private Double acturalCharge;
    private Double discount;
    private String invoiceNo;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date billFillDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date billConfirmDate;
    private String billStatus;
    private Integer creatorId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    private Date createTimeStart;
    private Date createTimeEnd;
    private Integer orgId;
    private Integer deptId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date invoiceTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date verifyTime;
    
    private Integer coopId;
    private String coopName;
    private String settlementPeriod;
    private String billName;
    
    private String fillUser;
    private Integer fillNumber;
    private String fillUrl;
    
    private Double invoiceAmount;

    private String billNumber;

    private String settlementModName;
    private String quantityConfirmName;
    private String billConfirmName;

    private List<CoopBillGroupDetail> services;

    private Integer statementId;
    private String paymentMethod;
    private String fillName;

    private String result;

    private Double acturalChargeOld;
    private Integer modifySaler;
    private String isDetail;
    private String serviceName;

    private String remark;
    private String departureStation;
    private String arrivalDeparturType;
    private String aircraftClassification;
    private String itCode;
    private String remarkSaler;
    private String rowUuid;
    
}
