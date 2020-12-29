package com.efreight.afbase.entity.procedure;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ReportReceivableAge implements Serializable {

    /**
     * 序号
     */
    private Integer sequence;
    /**
     * 业务范畴
     */
    private String businessScope;

    /**
     * 订单客户、收款客户
     */
    private Integer customerId;

    /**
     * 客户名称
     */
    private String coopName;

    /**
     * 责任销售
     */
    private String salesName;

    /**
     * 责任客服
     */
    private String servicerName;

    /**
     * 是否白名单
     */
    private Integer whiteValid;

    /**
     * 信用等级
     */
    private String creditLevel;

    /**
     * 信用期限
     */
    private String creditLimit;

    /**
     * 信用额度
     */
    private BigDecimal creditDuration;

    /**
     * 是否超额
     */
    private String durationValid;

    /**
     * 最大超期天数
     */
    private String overdueDays;

    /**
     * 是否超期
     */
    private String overdueValid;

    /**
     * 应收金额（本币）
     */
    private BigDecimal functionalAmount;

    /**
     * 账期内金额（本币）
     */
    @TableField(value = "no_functional_amount_writeoff_valid_0")
    private BigDecimal noFunctionalAmountWriteoffValid0;

    /**
     * 超期金额（本币）
     */
    @TableField(value = "no_functional_amount_writeoff_valid_1")
    private BigDecimal noFunctionalAmountWriteoffValid1;

    private Integer orgId;

    private Integer customerType;

    private String customerName;

    private String countRanges;
    
    private String orgEditionName;
    
    private Integer otherOrg;

}
