package com.efreight.ws.afbase.pojo.order.list;

import lombok.Data;

import java.io.Serializable;

@Data
public class Order implements Serializable {
    private String awbNumber;
    private String orderCode;
}
