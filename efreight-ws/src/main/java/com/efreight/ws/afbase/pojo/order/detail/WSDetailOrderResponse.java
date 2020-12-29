package com.efreight.ws.afbase.pojo.order.detail;

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
public class WSDetailOrderResponse extends WSResponse implements Serializable {
    private OrderDetail orderDetail;

    @XmlElement(name = "shipperLetter")
    @XmlElementWrapper(name = "shipperLetterList")
    private List<ShipperLetter> shipperLetterList;

    @XmlElement(name = "orderIncome")
    @XmlElementWrapper(name = "orderIncomeList")
    private List<OrderIncome> orderIncomeList;

    @XmlElement(name = "orderCost")
    @XmlElementWrapper(name = "orderCostList")
    private List<OrderCost> orderCostList;
}
