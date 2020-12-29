package com.efreight.ws.afbase.pojo.order.detail;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class OrderCost implements Serializable {
    private Date financialDate;
    private String customerName;
}
