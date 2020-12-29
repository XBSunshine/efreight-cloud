package com.efreight.afbase.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_awb_route_api_message")
public class AfAwbRouteApiMessage implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@TableId(value = "api_id", type = IdType.AUTO)
    private Integer  apiId;
	//MawbNum 运单号
	private String mawbNum;
    //Hawb 分单号
	private String hawb;
    //ErrorState 异常状态代码
	private String errorState;
    //FlightStatus 轨迹类型代码
	private String flightStatus;
    //StatusText 轨迹内容描述
	private String statusText;
    //MftRecCode 海关舱单回执代码
	private String mftRecCode;
    //DeclareCode 报关单单号
	private String declareCode;
    //Batch  批次
	private String batch;
    //SourceSyscode  主键
	private String sourceSyscode;
    //FlightNumber 航班号/航次号
	private String flightNumber;
    //FlightDate 航班日期
	private String flightDate;
    //TakeoffTime 起飞轨迹（DEP）中的轨迹发生时间
	private String takeOffTime;
    //LandingTime 降落轨迹中的轨迹发生时间
	private String landingTime;
    //EventDateTime 轨迹发生时间
	private String eventDateTime;
    //EventLocationCode 轨迹发生地点
	private String eventLocationCode;
    //Origin  轨迹始发港
	private String origin;
    //Destination 轨迹目的港 
	private String destination;
    //ShipmentQuantity 件数
	private Integer shipmentQuantity;
    //QuantityUnit  件数单位
	private String quantityUnit;
    //GrossWeight  轨迹中的货物 毛重
	private BigDecimal grossWeight;
    //GrossWeightUnit  毛重单位
	private String grossWeightUnit;
	
    //FlightRemark 备注
	private String flightRemark;
    //MessageTransmissionTime 获取轨迹时间 
	private String messageTransmissionTime;
	//回执系统标识
	private String statusType;
	//回执删除状态标识
	private String psStatus;
	
	private LocalDateTime createTime;
	
	private String IEFlag;

}
