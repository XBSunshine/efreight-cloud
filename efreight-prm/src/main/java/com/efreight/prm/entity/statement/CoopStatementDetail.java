package com.efreight.prm.entity.statement;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lc
 * @date 2021/1/29 17:21
 */
@Data
public class CoopStatementDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据ID
     */
    private Integer statementId;
    /**
     * 账单名称
     */
    private String statementName;
    /**
     * 状态
     */
    private String statementStatus;
    /**
     * 账单月份
     */
    private String statementDate;
    /**
     * 账单金额
     */
    private String amountReceivable;

    /**
     * 未核销金额
     */
    private String  unverifiedAmount;

    /**
     * 业务区域
     */
    private String billTemplate;
    /**
     * 销售确认人
     */
    private String confirmSalerName;
    /**
     * 客户确认时间
     */
    private String confirmCustomerTime;
    /**
     * 开票客户名称
     */
    private String invoiceTitle;
    /**
     * 发票类型
     */
    private String invoiceType;
    /**
     * 发票号
     */
    private String invoiceNumber;
    /**
     * 开票人
     */
    private String invoiceUserName;
    /**
     * 开票日期
     */
    private String invoiceDate;
    /**
     * 核销人
     */
    private String invoiceWriteoffUserName;
    /**
     * 核销日期
     */
    private String invoiceWriteoffDate;
    /**
     * 销售金额
     */
    private String invoiceWriteoffAmount;
    /**
     * 快递号
     */
    private String expressNumber;
    /**
     * 发送账单
     */
    private String sendBill;
    /**
     * 电子发票接收邮箱
     */
    private String invoiceMailTo;

}
