package com.efreight.afbase.entity;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class BranchLine implements Serializable {
    private static final long serialVersionUID = 1L;

    //主运单号
    private String awbnumber;
    private String numberid;
    private String hwbnumber;
    private String rearchid;
    private String totalpiecequantity;
    private String totalgrossweight;
    private String totalvolumnamount;
    private String flightno;
    private String flightdate;
    private String departure;
    private String destination;
    private String goodsname;
    private String goodscnname;
    private String specialgoodscode;
    private String customscode;
    private String transportmode;
    private String freightpaymentmethod;
    private String cneecity;
    private String cneecountry;
    private String mft2201status;
    private String mft3201status;
    private String mft9999status;
    private String mft4201status;
    private String mft5202status;
    private String response;
  
//
    private String oldrearchid;
    private String begindate;
    private String enddate; 
}
