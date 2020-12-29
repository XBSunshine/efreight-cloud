package com.efreight.prm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CoopBillGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    private String coopName;
    private Integer coopId;
    private String billId;
    private String billName;
    private String coopNameAndBillName;
    private List<CoopBillGroupDetail> coopBillGroupDetails;
    private Boolean hasChildren;

    /*private String coopName1;//仅用于查询
    private String billName1;//仅用于查询
    private String settlementModName;
    private String billNumber;
    private String quantityConfirmName;
    private String billConfirmName;
    private Integer orgId;*/

}
