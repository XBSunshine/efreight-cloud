package com.efreight.afbase.entity;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class FhlLook implements Serializable {

    private static final long serialVersionUID = 1L;

    private String seq;
    private String dashboardId;
    private String forwarder;
    private String mawbCode;
    private String hawbCode;
    private String orderCode;
    private String hawbCount;
    private String departure;
    private String destination;
    private String existsMftt2201;
    private String mft2201Pieces;
    private String mft2201Weight;
    private String mft2201Flightno;
    private String  mft2201Flightdate;
    private String terminalName;
    private String mft2201Time;
    private String mft2201Recv;
    private String mft2201Status;
    private String mft9999Recv;
    private String mft3201Recv;
    private String mft9993Recv;
    private String mft99935Recv;
    private String opDate;
    private String opLableTime;
    private String sliSendTime;
    private String sliInboundTime;
    private String sliPiece;
    private String sliWeight;
    private String sliVolume;
    private String fwbSendTime;
    private String fwbRecv;
    private String fwbStatus;
    private String awbPiece;
    private String awbWeight;
    private String customerName;
    private String inboundCode;
    private String inboundDate;
    private String veName;
    private String veTerminal;
    private String veSendTime;
    private String veRecv;
    private String veStatus;
    private String terminalSendTime;
    private String terminalStatus;
    private String acDepPiece;
    private String acDepWeight;
    private String acDepFlightno;
    private String acDepFlightdate;
    private String acDepText;
    private String mft2201Optype;
    private String sliStatus;
    private String sliRecv;
    private String acRcsPiece;
    private String acRcsWeight;
    private String acRcsFlightno;
    private String acRcsFlightdate;
    private String acRcsText;
    private String acRcsTime;
    private String acFohTime;
    private String awbPrintsliTime;
    private String vsStatus;
    private String vsArrivalTime;
    //
   

}
