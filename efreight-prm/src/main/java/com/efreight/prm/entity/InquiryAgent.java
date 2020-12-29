package com.efreight.prm.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * PRM 询盘代理
 * </p>
 *
 * @author qipm
 * @since 2020-05-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
//@TableName("prm_inquiry_agent")
public class InquiryAgent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer inquiryId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 业务范畴
     */
    private String businessScope;

    /**
     * 询盘代理ID
     */
    private Integer inquiryAgentId;
    private String inquiryAgentName;
    private String coopCode;

    /**
     * 询盘代理简称
     */
    private String inquiryAgentNameShort;

    /**
     * 优势航司代码，逗号分隔拼接
     */
    private String carrierCode;
    private List<String> carrierCodes;
    /**
     * 签约类型
     */
    private String contractType;

    /**
     * 始发港代码，逗号分隔拼接
     */
    private String departureStation;
    private List<String> departureStations;

    /**
     * 目的港代码，逗号分隔拼接
     */
    private String arrivalStation;
    private List<String> arrivalStations;

    /**
     * 目的国代码，逗号分隔拼接
     */
    private String nationCodeArrival;
    private List<String> nationCodeArrivals;

    /**
     * 航线
     */
    private String routingName;

    /**
     * 订舱联系人，逗号分隔拼接
     */
    private String bookingContactsId;
    private String bookingContactsName;
    private List<Integer> orderContacts;
    /**
     * 备注
     */
    private String remark;
    private Integer isValid;

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

    private Integer inquiryOrderAmount;

    private String columnStrs;

}
