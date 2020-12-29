package com.efreight.afbase.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * VIEW
 * </p>
 *
 * @author xiaobo
 * @since 2020-12-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_V_currency_rate")
public class VCurrencyRate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 币种代码
     */
    private String currencyCode;

    /**
     * 汇率
     */
    private BigDecimal currencyRate;

    /**
     * 币种名称
     */
    @TableField(exist = false)
    private String currencyName;

    /**
     * 币种数字代码
     */
    @TableField(exist = false)
    private String currencyNum;


}
