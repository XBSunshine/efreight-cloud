package com.efreight.prm.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CoopBillGroupMerge implements Serializable {
    private static final long serialVersionUID = 1L;

    private String coopName;
    private Integer coopId;
    private String billName;
    private String coopNameAndBillName;
    private List<CoopBillGroupDetail> coopBillGroupDetails;
    private String settlementModName;
    private String billNumber;
    private String quantityConfirmName;
    private String billConfirmName;
    private Integer orgId;
    private Integer billId;
    private String settlementType;
    private String settlementPeriod;
    private Double acturalCharge;
    private String billStatus;
    private Integer fillNumber;
    private Integer settlementId;
    private Double planCharge;
    private Double discount;
    private String paymentMethod;
    private String fillUrl;
    private String fillName;

}
