package com.efreight.prm.entity.writeoff;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lc
 * @date 2021/3/16 16:49
 */
@Data
public class WriteOffList implements Serializable {

    private static final long serialVersionUID = 1L;

    private String rowUuid;
    private String statementRowUuid;
    private Integer writeOffId;

    private String writeOffNum;
    private String statementName;
    private String statementDate;
    private String zoneCode;
    private String zoneName;
    private String invoiceNumber;
    private String invoiceTitle;
    private String invoiceDate;
    private String amountReceived;
    private String amountWriteOff;
    private String writeOffDate;
    private String writeOffUser;
    private String optTime;
    private String financialAccountCode;
    private String financialAccountName;
    private String writeOffRemark;

}
