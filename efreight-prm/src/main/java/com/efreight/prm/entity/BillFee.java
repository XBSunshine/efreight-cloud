package com.efreight.prm.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class BillFee implements Serializable {
    private String feeName;
    private String feeType;
    private Double unitPrice;
    private Double amount;
    private Double sumCharge;
}
