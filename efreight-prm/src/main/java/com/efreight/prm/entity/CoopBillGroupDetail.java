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
public class CoopBillGroupDetail implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer billId;
    private String settlementType;
    private String settlementPeriod;
    private Double acturalCharge;
    private String coopName;
    private Integer coopId;
    private String billName;
    private String billNumber;
    private String billStatus;
    private String quantityConfirmName;
    private String billConfirmName;
    private String coopNameAndBillName;
    private String settlementModName;
    private Integer fillNumber;
    private Integer settlementId;
    private String coopName1;
    private Double planCharge;
    private Double discount;
    private String paymentMethod;
    private String fillUrl;
    private String fillName;
    private Boolean hasChildren;
}
