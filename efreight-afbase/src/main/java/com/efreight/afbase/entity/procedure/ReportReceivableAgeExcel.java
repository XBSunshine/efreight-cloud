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
public class ReportReceivableAgeExcel implements Serializable {

    /**
     * 业务范畴
     */
    private String businessScope;

    private String coopCode;

    /**
     * 客户名称
     */
    private String coopName;
    /**
     * 责任客服
     */
    private String servicerName;
    /**
     * 责任销售
     */
    private String salesName;
    /**
     * 是否白名单
     */
    private String whiteValid;
    /**
     * 信用等级
     */
    private String creditLevel;
    /**
     * 信用额度
     */
    private String creditLimit;


    /**
     * 结算周期(EQ)
     */
    private String settlementPeriod;

    /**
     * 信用期限
     */
    private String creditDuration;
    /**
     * 最大超期天数
     */
    private String overdueDays;


    /**
     * 应收金额（本币）
     */
    private String functionalAmount;


    /**
     * 账期内金额（本币）
     */
    private String noFunctionalAmountWriteoffValid0;

    /**
     * 超期金额（本币）
     */

    private String noFunctionalAmountWriteoffValid1;

    private String colName_1;
    private String colName_2;
    private String colName_3;
    private String colName_4;
    private String colName_5;
    //以下是备用字段  后面可以优化成动态添加对象属性
    private String colName_6;
    private String colName_7;
    private String colName_8;
    private String colName_9;
    private String colName_10;
    private String colName_11;
    private String colName_12;
    private String colName_13;

}
