package com.efreight.afbase.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OperationPlanExcel {

	private  String numberCode;
	private  String coopCode;
	private  String coopName;
	/**
     * 主单号
     */
    private String awbNumber;
    /**
     * 客户单号
     */
    private String customerNumber;
    /**
     * 航班信息
     */
    private String arrivalStation;
    /**
     * 件/毛/体
     */
    private String planPieces;
    /**
     * 货物情况
     */
    private String goodsNameCn;
    /**
     * 操作信息
     */
    private String hawbQuantity;
    /**
     * 责任客服
     */
    private String servicerName;
    /**
     * 备注
     */
    private String remark;
    
   
   
}
