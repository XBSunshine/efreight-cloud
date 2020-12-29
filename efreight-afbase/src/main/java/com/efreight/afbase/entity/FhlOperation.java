package com.efreight.afbase.entity;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class FhlOperation implements Serializable {

    private static final long serialVersionUID = 1L;
    //主运单号
    private String syscode;
    private String awbnumber;
    private String hwbnumber;
    private String numberid;
    private String inputdate;
    private String createdate;
    private String billtype;
    private String totalpiecequantity;
    private String piecequantity;
    private String totalgrossweight;
    private String totalvolumnamount;
    private String flightno;
    private String flightdate;
    private String departure;
    private String destination;
    private String goodsname;
    private String forwarder;
    private String origin;
    private String awbtype;
    private String awbtypename;
    private String businesstype;
    private String businessname;
    private String isediawb;
    //原始状态
    private String mftstatus;
    //原始回执
    private String mftresponse;
    //理货状态
    private String tallystatus;
    //理货回执
    private String tallyresponse;
    private String shpcode;
    private String shipper;
    private String shpaddress;
    private String shpcountrycode;
    private String shptelephone;
    
    private String cnecode;
    private String consignee;
    private String cneaddress;
    private String cnecountrycode;
    private String cnetelephone;
    private String cnecontactname;
    private String cnecontacttelephone;
    private String nfycode;
    private String nfyname;
    private String nfyaddress;
    private String nfytelephone;
    
    //申报原始状态
    private String ismftdeclare;
    //申报理货状态
    private String istallydeclare;
    private String declaredate;
    private String carrierid;
    private String arrivaldatetime;
    private String departuredatetime;
    private String loadingdate;
    private String arrivaldate;
    private String methodcode;
    private String transhipmentlocationid;
    private String transitdestinationid;
    private String transportsplitindicator;
//
    private String startdate;
    private String enddate; 
    private String messageType; 
}
