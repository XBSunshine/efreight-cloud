package com.efreight.ws.afbase.pojo.order.list;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class ListOrderRequest implements Serializable {
    @XmlElement(required = true)
    private String flightDateStart;
    @XmlElement(required = true)
    private String flightDateEnd;
}
