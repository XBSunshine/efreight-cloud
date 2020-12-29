package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 币种
 * </p>
 *
 * @author wangxx
 * @since 2019-07-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_currency")
public class Currency implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 币种代码
     */
    private String currencyCode;

    /**
     * 币种名称
     */
    private String currencyName;

    /**
     * 币种符号
     */
    private String currencySign;

    /**
     * 币种数字代码
     */
    private String currencyNum;

    /**
     * 常用币种
     */
    private Boolean currencyCommon;

}
