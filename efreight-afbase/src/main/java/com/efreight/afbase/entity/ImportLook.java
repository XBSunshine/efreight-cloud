package com.efreight.afbase.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @program:efreight-cloud
 * @description:
 * @author:shihongkai
 * @create:2021-01-12
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ImportLook  implements Serializable {

    private static final long serialVersionUID = 1L;
    private Integer orderId;
    //序号
    private String seq;
    //看板表id
    private String dashboardId;
    //货代六字码
    private String forwarder;
    //主单号
    private String mawbCode;
    //分单号
    private String hawbCode;
    //订单号
    private String orderCode;
    //分单数量
    private String hawbCount;
    //始发港
    private String departure;
    //目的港
    private String destination;
    //总件数
    private String totalPieces;
    //总重量
    private String totalWeight;
    //总体积
    private String totalVolume;
    //件数
    private String pieces;
    //重量
    private String weight;
    //体积
    private String volume;
    //货物中文品名
    private String goodCname;
    //货物英文品名
    private String goodEname;
    //是否存在原始，Y存在，其他不存在
    private String existsMft1201;
    //原始航班号
    private String mft1201Flightno;
    //原始航班日期
    private String mft1201Flightdate;
    //原始发送时间
    private String mft1201Time;
    //原始最新回执--空，未存
    private String mft1201Recv;
    //原始状态,SAVE/SUCC/ERROR/AUDIT/do-xxx
    private String mft1201Status;
    //原始回执时间
    private String mft1201RecvTime;
    //原始操作类型
    private String mft1201Optype;
    //放行回执，YES或空
    private String mft9999Recv;
    //是否存在理货，YES理货正常，NO理货异常，空无理货
    private String existsMft5201;
    //理货发送时间
    private String mft5201Time;
    //理货最新回执--空，未存
    private String mft5201Recv;
    //理货状态,SAVE/SUCC/ERROR/AUDIT/do-xxx
    private String mft5201Status;
    //理货回执时间
    private String mft5201RecvTime;
    //是否存在分拨运抵，YES分拨运抵正常，空无运抵
    private String existsMft3202;
    //分拨运抵发送时间
    private String mft3202Time;
    //分拨运抵最新回执--空，未存
    private String mft3202Recv;
    //分拨运抵状态,SAVE/SUCC/ERROR/AUDIT/do-xxx
    private String mft3202Status;
    //分拨运抵回执时间
    private String mft3202RecvTime;
    //分拨申请，YES分拨审核通过，空无分拨申请
    private String mft6202Recv;
    //运单发送时间
    private String fwbSendTime;
    //运单回执
    private String fwbRecv;
    //运单状态，SUCC/ERROR/SEND
    private String fwbStatus;
    //运单件数
    private String awbPiece;
    //运单重量
    private String awbWeight;
}
