package com.efreight.prm.entity.statement;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * @author lc
 * @date 2021/2/4 14:10
 */
@Data
public class CoopStatementList implements Serializable {
    private static final long serialVersionUID = 1L;

    private String no;

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
     * 应收金额（本币）
     */
    private String amountReceived;


    /**
     * 账期内金额 = 应收金额 - 超期金额
     */
    public String accountPeriodAmount;

    /**
     * 超期金额(本币)
     */
    private String overdueAmount;


    /**
     * 是否超期  true 是  false 否
     */
    private boolean hasOverdueDays;

    /**
     * 是否超额 true是 false 否
     */
    private boolean hasOverdueAmount;

    /**
     * 区间金额1
     */
    private String intervalAmount1;
    /**
     * 区间金额2
     */
    private String intervalAmount2;
    /**
     * 区间金额3
     */
    private String intervalAmount3;
    /**
     * 区间金额4
     */
    private String intervalAmount4;
    /**
     * 区间金额5
     */
    private String intervalAmount5;

    /**
     * 区间金额6
     */
    private String intervalAmount6;

    public void transTo(CoopStatementBean bean, DecimalFormat decimalFormat){
        this.setScope(bean.getScope());
        this.setCoopId(bean.getCoopId());
        this.setCoopCode(bean.getCoopCode());
        this.setCoopName(bean.getCoopName());
        this.setBillTemplate(bean.getBillTemplate());
        this.setCustomerResponsible(bean.getCustomerResponsible());
        this.setWhitelistDate(bean.getWhitelistDate());
        this.setCreditLevel(bean.getCreditLevel());
        this.setCreditLimit(bean.getCreditLimit());
        this.setSettlementPeriod(bean.getSettlementPeriod());
        this.setCreditDuration(bean.getCreditDuration());
        this.setOverdueDays(bean.getOverdueDays() < 0 ? 0:bean.getOverdueDays());
        this.setAmountReceived(formatBigDecimal(decimalFormat, bean.getAmountReceived()));
        this.setAccountPeriodAmount(formatBigDecimal(decimalFormat, bean.getAccountPeriodAmount()));
        this.setOverdueAmount(formatBigDecimal(decimalFormat, bean.getOverdueAmount()));
        this.setHasOverdueDays(bean.getHasOverdueDays());
        this.setHasOverdueAmount(bean.getHasOverdueAmount());
        this.setIntervalAmount1(formatBigDecimal(decimalFormat, bean.getIntervalAmount1()));
        this.setIntervalAmount2(formatBigDecimal(decimalFormat, bean.getIntervalAmount2()));
        this.setIntervalAmount3(formatBigDecimal(decimalFormat, bean.getIntervalAmount3()));
        this.setIntervalAmount4(formatBigDecimal(decimalFormat, bean.getIntervalAmount4()));
        this.setIntervalAmount5(formatBigDecimal(decimalFormat, bean.getIntervalAmount5()));
        this.setIntervalAmount6(formatBigDecimal(decimalFormat, bean.getIntervalAmount6()));
    }

    private String formatBigDecimal(DecimalFormat decimalFormat, BigDecimal decimal){
        if(null == decimal || new BigDecimal(0).compareTo(decimal) == 0){
            return "0.00";
        }
        return decimalFormat.format(decimal.setScale(2, BigDecimal.ROUND_HALF_UP));
    }

}
