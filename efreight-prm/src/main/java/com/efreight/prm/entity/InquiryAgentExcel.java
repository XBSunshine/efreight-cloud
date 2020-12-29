package com.efreight.prm.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * PRM 询盘代理导出Excel
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class InquiryAgentExcel implements Serializable {

    private String coopCode;

    private String inquiryAgentName;

    /**
     * 询盘代理简称
     */
    private String inquiryAgentNameShort;

    /**
     * 优势航司代码，逗号分隔拼接
     */
    private String carrierCode;
    /**
     * 签约类型
     */
    private String contractType;

    /**
     * 始发港代码，逗号分隔拼接
     */
    private String departureStation;

    /**
     * 目的港代码，逗号分隔拼接
     */
    private String arrivalStation;

    /**
     * 目的国代码，逗号分隔拼接
     */
    private String nationCodeArrival;

    /**
     * 航线
     */
    private String routingName;

    /**
     * 订舱联系人，逗号分隔拼接
     */
    private String bookingContactsName;
    /**
     * 备注
     */
    private String remark;

    /**
     * 90天内发盘票数
     */
    private Integer inquiryOrderAmount;
}
