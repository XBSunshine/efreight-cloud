package com.efreight.hrs.entity;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DeptExcel implements Serializable {

    private String deptName;
    
    private String deptCode;

    private String shortName;

    private String fullName;

    private String managerName;

    private String isProfitunit;

    private String isFinalProfitunit;

    private Integer budgetHc;
    
    private Integer actualHc;

    private String deptStatus;


}
