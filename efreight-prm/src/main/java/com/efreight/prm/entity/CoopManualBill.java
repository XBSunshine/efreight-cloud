package com.efreight.prm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CoopManualBill {

    private Integer statementId;
    private Integer coopId;
    private String coopName;
    private Integer serviceId;
    private String serviceName;
    private String departureStation;
    private String statementName;
    private String toUsers;
    private String remark;
    private BigDecimal baseQuantity;
    private BigDecimal unitPrice;
    private BigDecimal totalCharge;
    private String statementDate;
    private String fillUrl;
    private String fillName;
    private String billTemplate;
    private String invoiceTitle;
    private String invoiceType;
    private String invoiceMailTo;
    private String invoiceRemark;
    private String billManualMailTo;
}
