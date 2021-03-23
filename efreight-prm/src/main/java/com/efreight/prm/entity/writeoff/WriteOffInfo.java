package com.efreight.prm.entity.writeoff;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lc
 * @date 2021/3/11 13:35
 * 发票核销信息
 */
@Data
public class WriteOffInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 账单ID
     */
    private Integer statementId;
    /**
     * 账单名称
     */
    private String statementName;
    /**
     * 账单日期
     */
    private String statementDate;
    /**
     * 抬头
     */
    private String invoiceTitle;
    /**
     * 发票号
     */
    private String invoiceNumber;
    /**
     * 发票金额
     */
    private String invoiceAmount;
    /**
     * 已核销金额
     */
    private String invoiceWriteOffAmount;
    /**
     * 未核销金额
     */
    private String unwrittenAmount;
}
