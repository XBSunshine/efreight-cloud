package com.efreight.ws.afbase.pojo.order.create;

import com.efreight.ws.common.pojo.WSResponse;
import lombok.Data;

import java.io.Serializable;

@Data
public class WSCreateOrderResponse extends WSResponse implements Serializable {
    private String orderCode;
}
