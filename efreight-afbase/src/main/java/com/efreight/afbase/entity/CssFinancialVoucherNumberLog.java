package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * CSS：凭证号日志
 * </p>
 *
 * @author xiaobo
 * @since 2021-01-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("css_financial_voucher_number_log")
public class CssFinancialVoucherNumberLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "voucher_number_log_id", type = IdType.AUTO)
    private Integer voucherNumberLogId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 财务：凭证日期
     */
    private LocalDateTime voucherDate;

    /**
     * 财务：凭证号
     */
    private Integer voucherNumber;

    /**
     * 凭证制作来源：收入成本、应收核销、应付核销、费用核销
     */
    private String voucherFrom;

    /**
     * 财务：凭证制作人
     */
    private Integer voucherCreatorId;

    /**
     * 财务：凭证制作人名称
     */
    private String voucherCreatorName;

    /**
     * 财务：凭证制作时间
     */
    private LocalDateTime voucherCreateTime;

    /**
     * 退回操作人
     */
    private Integer returnVoucherCreatorId;

    /**
     * 退回操作人名称
     */
    private String returnVoucherCreatorName;

    /**
     * 退回操作时间
     */
    private LocalDateTime returnVoucherCreateTime;


}
