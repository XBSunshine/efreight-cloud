package com.efreight.afbase.entity.route;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_awb_route_track_awb")
public class AfAwbRouteTrackAwb {

    //数据ID 自增长
    @TableId(value = "awb_route_track_awb_id", type = IdType.AUTO)
    private Integer trackAwbId;
    //轨迹表ID
    private Integer awbRouteId;
    //主单号
    private String awbNumber;
    //航班状态代码
    private String flightStatusCode;
    //航班状态名称
    private String flightStatusName;
    //航班号
    private String flightNum;
    //所在机场
    private String airportCode;
    //始发的港
    private String departureStation;
    //目的港
    private String arrivalStation;
    //批次号
    private String batchNumber;
    //事件时间
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm",timezone = "GMT+8")
    private Date eventTime;
    //备注
    private String remark;
    //件数
    private String quantity;
    //毛重
    private String grossWeight;
    //创建时间
    private Date createTime;
    //报文获取顺序码
    private String sourceSyscode;
}
