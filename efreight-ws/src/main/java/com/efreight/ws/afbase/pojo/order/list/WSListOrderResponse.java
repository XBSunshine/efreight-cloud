package com.efreight.ws.afbase.pojo.order.list;

import com.efreight.ws.common.pojo.WSResponse;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.io.Serializable;
import java.util.List;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class WSListOrderResponse extends WSResponse implements Serializable {
    @XmlElement(name = "order")
    @XmlElementWrapper(name = "orderList")
    private List<Order> orderLists;
}
