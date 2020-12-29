package com.efreight.afbase.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OperationPlanPrintTag {

    //主单号
    private String awbNumber;
    //订单号
    private String orderCode;
    //分单号
    private String hawbNumber;
    //航司两字码 主单号前3位对应航司的两字码
    private String carrierCode;
    //分单件数
    private Integer piecesHawb;
    //分单件数序号
    private Integer pieceNumber;
    //总流水号 可认为是范围截止号码的序号；四位字符，不够补0，例如  23 则 0023；
    private String sequence;
    //始发港 主单的始发港；任意一票订单
    private String departure;
    //主单目的港 订单的 目的港；
    private String destinatonAwb;
    //主单件数 主单托书件数；
    private Integer piecesAwb;
    //分单目的港 分单目的港；当前分单托书目的港；
    private String destinationHawb;

}
