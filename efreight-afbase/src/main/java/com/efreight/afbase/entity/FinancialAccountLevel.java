package com.efreight.afbase.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class FinancialAccountLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer financialAccountIdB;

    private Integer financialAccountIdC;

    private Integer financialAccountIdD;

    private Integer financialAccountIdE;

    private String financialAccountCodeB;

    private String financialAccountCodeC;

    private String financialAccountCodeD;

    private String financialAccountCodeE;


}
