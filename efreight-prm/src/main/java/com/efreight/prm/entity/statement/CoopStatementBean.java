package com.efreight.prm.entity.statement;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 应收账龄-列表
 * @author lc
 * @date 2021/1/29 13:38
 */
@Data
public class CoopStatementBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer coopId;

    /**
     * 业务范畴
     */
    private String scope;

    /**
     * 客户代码
     */
    private String coopCode;

    /**
     * 客户名称
     */
    private String coopName;

    /**
     * 业务区域
     */
    private String billTemplate;

    /**
     * 客户负责人
     */
    private String customerResponsible;

    /**
     * 白名单日期,用于判断是否为白名单
     */
    private String whitelistDate;

    /**
     * 信用等级
     */
    private String creditLevel;

    /**
     * 授信额度
     */
    private BigDecimal creditLimit;

    /**
     * EQ
     */
    private String settlementPeriod;

    /**
     * 授信期限（天）
     */
    private Integer creditDuration;

    /**
     * 超期天数
     */
    private Integer overdueDays;

    /**
     * 应收金额（本币） = 实收金额 - 核销金额
     */
    private BigDecimal amountReceived;

    /**
     * 账期内金额 = 授信期限内的账单的应收金额。其计算规则如下：
     * 方式一：应收金额 - 超期金额
     * 方式二：所有授信期限内的账单的应收金额相加
     */
    public BigDecimal accountPeriodAmount;

    /**
     * 超期金额(本币) = 超过授信期限的账单的应收金额。
     */
    private BigDecimal overdueAmount;


    /**
     * 是否超期  true 是  false 否
     */
    private Boolean hasOverdueDays;

    /**
     * 是否超额 true是 false 否
     */
    private Boolean hasOverdueAmount;

    /**
     * 区间金额1
     */
    private BigDecimal intervalAmount1;
    /**
     * 区间金额2
     */
    private BigDecimal intervalAmount2;
    /**
     * 区间金额3
     */
    private BigDecimal intervalAmount3;
    /**
     * 区间金额4
     */
    private BigDecimal intervalAmount4;
    /**
     * 区间金额5
     */
    private BigDecimal intervalAmount5;

    /**
     * 区间金额6
     */
    private BigDecimal intervalAmount6;
}
