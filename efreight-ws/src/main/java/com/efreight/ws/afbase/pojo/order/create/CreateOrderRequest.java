package com.efreight.ws.afbase.pojo.order.create;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class CreateOrderRequest implements Serializable {
    @XmlElement(required = true)
    private String coopCode;
    @XmlElement(required = true)
    private String storehouseCode;
    @XmlElement(required = true)
    private String departureStation;
    @XmlElement(required = true)
    private String arrivalStation;
    @XmlElement(required = true)
    private String expectDeparture;
    @XmlElement(required = true)
    private Integer planPieces;
    @XmlElement(required = true)
    private BigDecimal planWeight;
    @XmlElement(required = true)
    private Double planVolume;
    @XmlElement(required = true)
    private Double freightUnitPrice;
    @XmlElement(required = true)
    private String consigneePrintRemark;
    private String consignorPrintRemark;
    @XmlElement(required = true)
    private String serviceName;;
    @XmlElement(required = true)
    private String salesName;
    @XmlElement(required = true)
    private String awbNumber;
    private String operationRemark;
    private String freightProfitRatioRemark;
    private String orderRemark;
    private String priceRemark;

}
