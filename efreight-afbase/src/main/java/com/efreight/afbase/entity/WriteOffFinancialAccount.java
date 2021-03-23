package com.efreight.afbase.entity;

import lombok.Data;

/**
 * @author lc
 * @date 2021/3/11 13:39
 * 发票核销-科目
 */
@Data
public class WriteOffFinancialAccount {

    private static final long serialVersionUID = 1L;

    /**
     * 科目名称
     */
    private String accountName;
    /**
     * 科目代码
     */
    private String accountCode;

    /**
     * 科目类型
     */
    private String accountType;
}
