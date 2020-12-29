package com.efreight.ws.afbase.pojo.order.detail;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Date;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class OrderDetail implements Serializable {
    @XmlTransient
    private Integer orderId;
    private String orderCode;
    private String awbNumber;
    private String businessProduct;
    private Date receiptDate;
    private String expectFlight;
    private String expectDeparture;
    private String departureStation;
    private String arrivalStation;
    private String warehouseCode;
    private String storehouseCode;
    private String goodsNameCn;
    private String goodsNameEn;
    private String goodsType;
    private String batteryType;
    private String packageType;
    private String orderRemark;
    private Integer planPieces;
    private Double planWeight;
    private Double planVolume;
    private Double planChargeWeight;
    private String planDimensions;
    private String saleCurrecnyCode;
    private String salePriceType;
    private Double saleMoney;
    private String freightProfitRatioRemark;
    private String msrCurrecnyCode;
    private String msrType;
    private Double msrMoney;
    private String msrProfitRatioRemark;
    private String priceRemark;
    private String servicerName;
    private String salesName;
    private String notifierRemark;
    private String arrivalAgent;
    private String handlingInfo;
    private String shippingMarks;
    private String airborneDocument;
    private String appraisalNote;
    private String switchAwbService;
    private String switchAwbDate;
    private String switchAwbAddress;
    private String switchAwbRemark;
    private String warehouseService;
    private String warehouseOperator;
    private Date inboundDate;
    private Date outboundDate;
    private String damageRemark;
    private String operationRemark;
    private String customsClearanceService;
    private String customsDeclaresType;
    private String customsClearanceCompany;
    private String customsInspectionDate;
    private Date customsClearanceDate;
    private String customsDeclaresRemark;
    private String customsInspectionRemark;
    private String deliveryService;
    private String deliveryCompany;
    private String deliveryDriver;
    private String deliverySigner;
    private String deliveryAddress;
    private Date deliveryDate;
    private Date deliverySignDate;
    private String deliveryRemark;
    private String arrivalCustomsClearanceService;
    private String arrivalCustomsClearanceCompany;
    private String arrivalCustomsInspectionDate;
    private Date arrivalCustomsClearanceDate;
    private String arrivalCustomsDeclaresRemark;
    private String arrivalCustomsInspectionRemark;
    private String pickUpDeliveryService;
    private String pickUpDeliveryCompany;
    private String pickUpAddress;
    private Date pickUpDeliveryDate;
    private String pickUpDeliveryAddress;
    private String pickUpDeliveryRemark;
    private String outfieldService;
    private String outfieldDeliveryCompany;
    private String outfieldTruckNumber;
    private String outfieldDriver;
    private String buildUpCompany;
    private String outfieldRemark;
    private String consigneeName;
    private String consigneeAddress;
    private String consigneeCode;
    private String consigneeCodeType;
    private String consigneeAeoCode;
    private String consigneeNationCode;
    private String consigneeStateCode;
    private String consigneeCityCode;
    private String consigneePostCode;
    private String consigneeTelNumber;
    private String consigneeFaxNumber;
    private String consigneesPrintRemark;
    private String consignorName;
    private String consignorAddress;
    private String consignorCode;
    private String consignorCodeType;
    private String consignorAeoCode;
    private String consignorNationCode;
    private String consignorStateCode;
    private String consignorCityCode;
    private String consignorPostCode;
    private String consignorTelNumber;
    private String consignorFaxNumber;
    private String consignorsPrintRemark;

}
