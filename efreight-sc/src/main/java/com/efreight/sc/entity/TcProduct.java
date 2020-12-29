package com.efreight.sc.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tc_product")
public class TcProduct implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@TableId(value = "product_id", type = IdType.AUTO)
	private Integer productId;
	@TableField(strategy = FieldStrategy.IGNORED)
	private Integer orgId;
	
	private String businessScope;
	
	private String productType;
	
	private String productName;
	@TableField(strategy = FieldStrategy.IGNORED)
	private Integer BookingAgentId;
	
	@TableField(strategy = FieldStrategy.IGNORED)
	private BigDecimal freightUnitprice;
	
	@TableField(strategy = FieldStrategy.IGNORED)
	private BigDecimal freightAmount;
	
	private String freightCurrecnyCode;
	
	@TableField(strategy = FieldStrategy.IGNORED)
	private BigDecimal msrUnitprice;
	
	@TableField(strategy = FieldStrategy.IGNORED)
	private BigDecimal msrAmount;
	
	private String msrCurrecnyCode;
	
	private String departureStation;
	
	private String arrivalStation;
	
	private String exitPort;
	
	private String transitStation;
	
	private String productRemark;
	@TableField(strategy = FieldStrategy.IGNORED)
	private Integer transitDays;
	
	private boolean productStatus;
	
	private Integer creatorId;
	
	private String creatorName;
	
	private LocalDateTime creatTime;
	@TableField(exist = false)
	private String freightType;
	@TableField(exist = false)
	private BigDecimal freightPrice; 
	@TableField(exist = false)
	private String msrType;
	@TableField(exist = false)
	private BigDecimal msrPrice;
	@TableField(exist = false)
	private String bookingAgentName;
	
	private String containerMethod;
	
}

