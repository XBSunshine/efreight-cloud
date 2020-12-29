package com.efreight.afbase.entity.procedure;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CssPReportSettleAfProcedure {

	private Integer orgId;
	private String businessScope;
	private String awbNumber;
	private String orderCode;
	//2.结算日期
	private String financialDate;
	//4.客户名称
	private String coopName;
	//6.运单来源
    private String awbFrom;
    //7.始发港
    private String departureStation;
	//8.目的港
    private String arrivalStation;
    //9.航班号
    private String expectFlight;
	//10.航线
    private String routingName;
	//11.航班日期
    private String expectDeparture;

	//12.服务产品
    private String businessProduct;
	//13.责任销售
    private String salesName;
	//14.责任客服
    private  String servicerName;
	//15.货物类型
	private String goodsType;
	
	//16.件数
	private Integer pieces;  
	//17.计重
	private String chargeWeight;
	//18.应收金额（原币）
	private String incomeAmount;
	//19.应付金额（原币）
	private String costAmount;
	//20.应收金额 (本币)	
	private String incomeFunctionalAmount;
	//21.应付金额（本币）
	private String costFunctionalAmount;
	//22.毛利（本币）
	private String grossProfit;
	//23.单公斤毛利
	private String grossProfitWeight;	
	
	
    //航班开始日期
    private String flightDateStart;
    //航班结束日期
    private String flightDateEnd;
    //毛利状况
    private String grossProfitStr;
	//运单来源类型
    private String awbFromType;
    //运单来源名称
    private String awbFromName;
    //结算日期开始时间
    private String financialDateStart;
    //结算日期结束时间
    private String financialDateEnd;

    //锁账状态
	private String orderStatus;
	
	//船次号
	private String shipVoyageNumber;
	//销售部门
	private String salesDep;
	private String columnStrs;
	//客户单号
	private String customerNumber;
	//显示毛利构成
	private Boolean showConstituteFlag;
	private String goodsSourceCode;
	
	private String exitPort;
	
	private String shippingMethod;
	private String businessMethod;

	private Integer orderPermission;

	private Integer currentUserId;

	private String reportType;
	
	private Integer otherOrg;

	private String routingPersonName;

	private String transitStation;

	@TableField("transit_station_2")
	private String transitStation2;
}
