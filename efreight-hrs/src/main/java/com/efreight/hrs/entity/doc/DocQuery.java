package com.efreight.hrs.entity.doc;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lc
 * @date 2021/1/21 17:09
 */
@Data
public class DocQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer orgId;
    private String businessScope;
    private String docName;
    private String number;
    private String uploadTimeStart;
    private String uploadTimeEnd;
    private String orderCode;
    private String customerNumber;
    private String dateTimeStart;
    private String dateTimeEnd;
    private String seller;
    private String customerService;
    private String operator;
    private String customer;
    private String docType;
    private String docOperator;

    private Integer current;
    private Integer size;
    private Integer total;

    private String columnStrs;
}
