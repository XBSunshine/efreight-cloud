package com.efreight.ws.afbase.pojo.order.inbound;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class InboundOrderRequest implements Serializable {
    @XmlElement(required = true)
    private String orderCode;
    @XmlElement(required = true)
    private String awbNumber;
    @XmlElement(required = true)
    private Integer orderPieces;
    @XmlElement(required = true)
    private BigDecimal orderGrossWeight;
    @XmlElement(required = true)
    private Double orderVolume;
    private String orderVolumeWeight;
    @XmlElement(required = true)
    private Double orderChargeWeight;
    @XmlElement(required = true)
    private String receiptDate;
    @XmlElement(required = true)
    private String warehouseCode;
}
