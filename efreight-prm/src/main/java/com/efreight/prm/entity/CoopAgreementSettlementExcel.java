package com.efreight.prm.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CoopAgreementSettlementExcel {
    private String billTemplate;
    private String coopName;
    private String coopCode;
    private String departureStation;
    private String arrivalDeparturType;
    private String paramText;
    private String serviceName;
    private String itCode;
    private String chargeStandard;
    private String paymentMethod;
    private String settlementPeriod;
    private String settlementType;
    private String chargeRemark;
    private String chargeDate;
    private String quantityConfirmName;
    private String billConfirmName;
    private String validUserName;
    private String headOfficeConfirmName;
    private String settlementState;
    private String needEmail;
    private String startChargeTime;
    private String reviewItNeed;
}
