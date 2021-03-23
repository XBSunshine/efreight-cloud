package com.efreight.prm.entity.writeoff;

import lombok.Data;

import java.io.Serializable;
import java.util.Optional;

/**
 * @author lc
 * @date 2021/3/16 16:49
 */
@Data
public class WriteOffQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private String statementDateBegin;
    private String statementDateEnd;
    private String coopName;
    private String writeOffTimeBegin;
    private String writeOffTimeEnd;

    private String zoneCode;
    private String invoiceNumber;
    private String statementName;
    private String optTimeBegin;
    private String optTimeEnd;

    private String writeOffUser;
    private String writeOffNum;
    private String invoiceTitle;

    private String invoiceDateBegin;
    private String invoiceDateEnd;

    private Integer current;
    private Integer size;
    private Integer offset;

    private String columnStrs;

    public Integer getOffset(){
        return (Optional.ofNullable(this.current).orElse(1) -1) * Optional.ofNullable(this.size).orElse(10);
    }
}
