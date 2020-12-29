package com.efreight.afbase.entity;


import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
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
@TableName("af_awb_route")
public class AfAwbRoute implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@TableId(value = "awb_route_id", type = IdType.AUTO)
    private Integer awbRouteId;
	private String awbNumber;
	private String actualFlightNum;
	private String departureStation;
	private String arrivalStation;
	private LocalDate  actualDeparture;
	private LocalDate actualArrival;
	private Integer quantity;
	private BigDecimal grossWeight;
	private LocalDateTime createTime;
	private Integer isTrack;
	private LocalDateTime trackTime;
	

}
