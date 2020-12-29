package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_flight_voyage")
public class FlightDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 航班id
     */
    @TableId(value = "flight_voyage_id", type = IdType.AUTO)
    private Integer flightVoyageId;

    private  Integer flightId;
    private  String flightNumber;
    private  String weekNum;
    private  String departureStation;
    private  String transitStation;
    private  String arrivalStation;
    private  String aircraftTypePc;
    private  String aircraftTypeBn;
    private  String aircraftTypeRemark;
    private  String takeoffTime;
    private  String arrivalTime;
    private  Integer panelLevel;
    private  String id;
    private  String isSign;
    private  String cutoffTime;
    private  Boolean arrivalTime1;
    private  Boolean cutoffTime1;
   
   
}
