package com.efreight.afbase.entity.procedure;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ReportPayableAgeExcel implements Serializable {

    private String businessScope;
    private String coopCode;
    private String coopName;
    private String coopType;
    private String noFunctionalAmountWriteoff;
    private String colName_1;
    private String colName_2;
    private String colName_3;
    private String colName_4;
    private String colName_5;
    //扩展字段
    private String colName_6;
    private String colName_7;
    private String colName_8;
    private String colName_9;
    private String colName_10;
    private String colName_11;
    private String colName_12;
    private String colName_13;

}
