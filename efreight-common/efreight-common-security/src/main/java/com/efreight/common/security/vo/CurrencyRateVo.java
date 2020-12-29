package com.efreight.common.security.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CurrencyRateVo {

    /**
     * 签约公司id
     */
    private Integer orgId;
    /**
     * 币种代码
     */
    private String currencyCode;
    /**
     * 汇率
     */
    private String currencyRate;
    /**
     * 生效日期
     */
    private String beginDate;
    /**
     * 失效日期
     */
    private String endDate;
    /**
     * 创建人
     */
    private Integer creatorId;
    /**
     * 创建时间
     */

    private LocalDateTime createTime;
    /**
     * 创建人姓名
     */
    private String creatorName;
}
