package com.efreight.common.remoteVo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Service implements Serializable {
    private static final long serialVersionUID = 1L;
    //服务ID
    private Integer serviceId;
    //签约公司ID
    private Integer orgId;

    // 业务范畴
    private String businessScope;

    // 服务类别
    private String serviceType;

    // 服务代码
    private String serviceCode;
    //服务中文名称
    private String serviceNameCn;
    //服务英文名称
    private String serviceNameEn;
    // 服务备注
    private String serviceRemark;
    //销项税率
    private Double vatOutput;
    // 进项税率
    private Double vatInput;
    //是否常用
    private Boolean isFrequent;
    //是否有效
    private Integer isValid;
    //是否系统保留服务
    private Boolean isSys;
    //创建人ID
    private Integer creatorId;
    //创建人
    private String creatorName;
    //创建时间
    private LocalDateTime createTime;
    //修改人ID
    private Integer editorId;
    //修改人
    private String editorName;
    //修改时间
    private LocalDateTime editTime;

    private String value;
    private String incomeExchangeRate;
    private String costExchangeRate;

    //
    //默认应收：1 是 0 否
    private Integer defaultIncome;
    //收费标准
    private String incomeChargeStandard;
    //收费币种
    private String incomeCurrency;
    //收费单价
    private BigDecimal incomeUnitPrice;

    //默认应付：1 是 0 否
    private Integer defaultCost;
    //付费标准
    private String costChargeStandard;
    //付费币种
    private String costCurrency;
    //付费单价
    private BigDecimal costUnitPrice;

    private String incomeUnitPriceStr;
    private String costUnitPriceStr;
    //收入
    private BigDecimal incomeAmountMin;
    private BigDecimal incomeAmountMax;
    private Integer incomeAmountDigits;
    private String incomeAmountCarry;
    //应付
    private BigDecimal costAmountMin;
    private BigDecimal costAmountMax;
    private Integer costAmountDigits;
    private String costAmountCarry;
}
