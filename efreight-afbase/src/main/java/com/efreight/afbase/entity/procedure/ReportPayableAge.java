package com.efreight.afbase.entity.procedure;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ReportPayableAge implements Serializable {

    private String businessScope;

    private Integer customerId;

    private String customerType;

    private Integer orgId;

    private String customerName;

    private String countRanges;
    
    private String orgEditionName; 
    
    private Integer otherOrg;

    private String salesName;

    private Integer currentUserId;

    private Integer orderPermission;

}
