package com.efreight.afbase.entity.exportExcel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OrderInquiryQuotationExcel {

    private String orderInquiryCode;
    private String departureStation;
    private String arrivalStation;
    private String carrierCode;
    private String expectDeparture;
    private String flightRemark;
    private String planPieces;
    private String planWeight;
    private String planVolume;
    private String planChargeWeight;
    private String planDimensions;
    private String batteryType;
    private String goodsNameCn;
    private String planPrice;
    private String inquiryRemark;
    private String inquiryAgentIds;

    private String quotationCompanyName;
    private String transitStation;
    private String batchRemark;
    private String quotationEndDate;
    private String weightClass;
    private String densityClass;
    private String quotationRemark;
    private String quotationContacts;

    private String inquiryAgentNames;

}
