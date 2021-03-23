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
public class CoopUnConfirmBillDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private String coopName;
    private Integer statementId;
    private Integer coopId;
    private String statementStatus;
    private String statementNumber;
    private String statementDate;
    private String id;
    private Integer billId;
    private String billNumber;
    private String settlementModName;
    private String billStatus;
    private String billName;
    private String settlementType;
    private String settlementPeriod;
    private BigDecimal acturalCharge;
    private BigDecimal planCharge;
    private BigDecimal discount;
    private BigDecimal fillNumber;
    private String fillUrl;
    private String fillName;
    private String fillUser;
    private Integer serviceId;
    private String serviceName;
    private String quantityConfirmName;
    private String billConfirmName;
    private String paymentMethod;
    private Integer orgId;
    private Integer settlementId;
    private BigDecimal unitPrice;
    private BigDecimal minCharge;
    private BigDecimal maxCharge;
    private Integer modifySaler;
    private String headOfficeConfirmName;
    private Integer needEmail;
    private BigDecimal baseQuantity;
    private BigDecimal unitPriceExcessive;
    private String billTemplate;
    private String remark;
    private String departureStation;
    private String arrivalDeparturType;
    private String aircraftClassification;
    private String itCode;
    private BigDecimal originalCharge;
    private BigDecimal fillNumberOriginal;
    private String remarkSaler;
    private transient List<String> billStatus1;
    private Integer creatorId;
    private String creatorName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date billFillDate;
    private Integer isSendMailAuto;
    private BigDecimal settlementCharge;
    private String rowUuid;
}
