package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_flight")
public class Flight implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 航班id
     */
    @TableId(value = "flight_id", type = IdType.AUTO)
    private Integer flightId;

    
    private String flightNumber;
    private transient String weekNum;
    private String departureStation;
    private transient String transitStation;
    private transient String arrivalStation;
    private transient String aircraftTypePc;
    private transient String aircraftTypeBn;
    private transient String takeoffTime;
    private transient String arrivalTime;
    private transient String cutoffTime;
    private transient LocalDate flightDate;
    private transient Boolean isTrue;
    private transient List<FlightDetail> flightDetails;
    private transient String id;
    private transient String isSign;
    private LocalDate beginDate;
    private LocalDate endDate;
    /**
     * 创建人ID
     */
    private Integer creatorId;

    /**
     * 创建人名称
     */
    private String creatorName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改人ID
     */
    private Integer editorId;

    /**
     * 修改人名称
     */
    private String editorName;

    /**
     * 修改人时间
     */
    private Date editTime;


//    /**
//     * 航班号
//     */
//    private Integer flightNumber;
//
//    /**
//     * 运行班次
//     */
//    private Integer operationDays;
//
//    /**
//     * 始发港
//     */
//    private String departureStation;
//
//    /**
//     * 计划起飞时间
//     */
//    private String departureTime;
//
//    /**
//     * 目的港
//     */
//    private String arrivalStation;
//
//    /**
//     * 计划降落时间
//     */
//    private String arrivalTime;
//
//    /**
//     * 机型
//     */
//    private String aircraftType;
//
//    /**
//     * 生效日期
//     */
//    private LocalDateTime beginDate;
//
//    /**
//     * 失效日期
//     */
//    private LocalDateTime endDate;


}
