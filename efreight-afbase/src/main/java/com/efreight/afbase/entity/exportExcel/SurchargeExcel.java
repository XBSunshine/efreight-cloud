package com.efreight.afbase.entity.exportExcel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SurchargeExcel {
    /**
     * 航司代码
     */
    private String carrierCode;
    /**
     * 始发港
     */
    private String departureStation;

    /**
     * 目的港
     */
    private String arrivalStation;

    /**
     * 目的国
     */
    private String arrivalNationCode;

    /**
     * IATA分区
     */
    private String routingName;

    /**
     * 运单排序
     */
    private String awbSort;

    /**
     * 附加费代码
     */
    private String surchargeCode;

    /**
     * 附加费名称
     */
    private String surchargeName;
    /**
     * 单价
     */
    private String unitPrice;

    /**
     * 收费方式
     */
    private String chargeMethod;

    /**
     * 最低收费
     */
    private String chargeMin;

    /**
     * 最高收费
     */
    private String chargeMax;

    /**
     * 生效日期
     */
    private String beginDate;

    /**
     * 失效日期
     */
    private String endDate;

}
