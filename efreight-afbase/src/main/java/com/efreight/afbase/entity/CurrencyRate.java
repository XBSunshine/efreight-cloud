/*
 *
 * Author: zhanghw
 */

package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.efreight.common.core.utils.FormatUtils;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 汇率维护
 *
 * @author zhanghw
 * @date 2019-08-30 17:42:34
 */
@Data
@TableName("af_currency_rate")
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "汇率维护")
public class CurrencyRate extends Model<CurrencyRate> {
    private static final long serialVersionUID = 1L;

    /**
     * 汇率id
     */
    @TableId(type = IdType.AUTO)
    private Integer crId;
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
    private BigDecimal currencyRate;
    /**
     * 生效日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginDate;
    /**
     * 失效日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;
    /**
     * 创建人
     */
    private Integer creatorId;
    /**
     * 创建时间
     */

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /**
     * 创建人姓名
     */
    private String creatorName;

    public String getCreatorName() {
        return FormatUtils.formatCreator(this.creatorName);
    }
}
