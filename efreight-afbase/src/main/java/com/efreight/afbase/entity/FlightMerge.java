package com.efreight.afbase.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class FlightMerge {

    private  String flightNumber;
    private  String weekNum;
    private  String departureStation;
    private  String transitStation;
    private  String arrivalStation;
    private  String aircraftTypePc;
    private  String aircraftTypeBn;
    private  String takeoffTime;
    private  String arrivalTime;
    private  Integer panelLevel;
   
   
}
