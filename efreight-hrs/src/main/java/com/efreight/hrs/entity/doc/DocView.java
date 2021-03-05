package com.efreight.hrs.entity.doc;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lc
 * @date 2021/1/21 17:09
 */
@Data
public class DocView implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer orgId;
    private Integer orderId;
    private Integer orderFileId;
    private String orderCode;
    private String businessScope;
    private String fileType;
    private String fileName;
    private String number;
    private String customerNumber;
    private String dateTime;
    private String coopName;
    private String salesName;
    private String servicerName;
    private String creatorName;
    private String fileOperator;
    private String createTime;
    private String fileUrl;

}
