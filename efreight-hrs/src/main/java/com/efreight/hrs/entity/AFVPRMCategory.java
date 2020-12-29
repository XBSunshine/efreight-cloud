package com.efreight.hrs.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AFVPRMCategory implements Serializable {
    private String categoryName;
    private String paramRanking;
    private String paramText;
    private String remarks;
    private String EDICode1;
}
