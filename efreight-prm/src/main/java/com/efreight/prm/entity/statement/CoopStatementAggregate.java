package com.efreight.prm.entity.statement;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 应收账龄-合计
 * @author lc
 * @date 2021/1/29 14:18
 */
@Data
public class CoopStatementAggregate implements Serializable {

    List<CoopStatementList> coopStatementList;

    /**
     * 应收金额（本币）-总计
     */
    private String amountReceived;

    /**
     * 账期内金额 - 总计
     */
    public String accountPeriodAmount;
    /**
     * 超期金额(本币) - 总计
     */
    private String overdueAmount;

    /**
     * 区间金额 - 总计
     */
    private String intervalAmount1;
    /**
     * 区间金额 - 总计
     */
    private String intervalAmount2;
    /**
     * 区间金额 - 总计
     */
    private String intervalAmount3;
    /**
     * 区间金额 - 总计
     */
    private String intervalAmount4;
    /**
     * +区间金额 - 总计
     */
    private String intervalAmount5;
    /**
     * +区间金额 - 总计
     */
    private String intervalAmount6;
}
