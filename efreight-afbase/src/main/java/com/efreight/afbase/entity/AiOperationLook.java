package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AiOperationLook  implements Serializable {

    //订单号
    private String orderCode;
    //主单号
    private String awbNumber;
    //分单号
    private String hawbNumber;
    //件数
    private String planPieces;
    //重量
    private String planWeight;
    //体积
    private String planVolume;
    //航班号
    private String mft1201Flightno;
    //航班日期
    private String mft1201Flightdate;
    //始发地
    private String departureStation;
    //目的地
    private String arrivalStation;
    //英文品名
    private String goodsNameEn;
    //原始状态
    private String mft1201Status;
    //理货状态
    private String mft5201Status;
    //分拨申请
    private String mft6202Recv;
    //分拨运抵
    private String mft3202Status;
}