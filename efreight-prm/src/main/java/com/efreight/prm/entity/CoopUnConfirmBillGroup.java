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
public class CoopUnConfirmBillGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    private String coopName;
    private Integer statementId;
    private Integer coopId;
    private String statementStatus;
    private String statementNumber;
    private String statementDate;
    private String id;
    private String serviceName;
    private String billStatus;
    private String billName;
    private String billNumber;
    private String isParent;
    private Double minCharge;
    private Double maxCharge;

    private Double planCharge;//应收金额
    private Double acturalCharge;//实收金额
    private Double discount;//折扣
    private String editorName;
    private Integer editorId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date editTime;
    private String isModify;
    private Integer statementMailSenderId;
    private String statementMailSenderName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date statementMailDate;
    private Double totalActuralCharge;
    private Integer modifySaler;
    private String confirmHeadOfficeName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date confirmHeadOfficeTime;
    private String confirmSalerName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date confirmSalerTime;
    private String headOfficeConfirmName;
    private String billConfirmName;
    private Integer transactorId;
    private List<CoopUnConfirmBillDetail> coopUnConfirmBillDetail;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date confirmCustomerTime;
    private String confirmCustomerName;
    private Integer settlementId;
    private String billManualMailTo;
    private Integer isSendMailAuto;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date mailSendTime;
    private Double acturalChargeAll;//同一个账单所有账单明细实收金额总和
}
