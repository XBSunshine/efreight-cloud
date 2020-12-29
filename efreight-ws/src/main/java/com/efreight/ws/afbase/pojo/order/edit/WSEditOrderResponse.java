package com.efreight.ws.afbase.pojo.order.edit;

import com.efreight.ws.afbase.contant.AFConstant;
import com.efreight.ws.common.pojo.WSResponse;
import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@Data
@XmlRootElement(name = "wSEditOrderResponse", namespace = AFConstant.ORDER_NAMESPACE)
public class WSEditOrderResponse extends WSResponse implements Serializable {
    private String orderCode;
}
