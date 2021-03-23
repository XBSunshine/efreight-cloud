package com.efreight.prm.entity.writeoff;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author lc
 * @date 2021/3/11 14:20
 * 核销单
 */
@Data
@TableName("prm_coop_statement_writeoff")
public class CoopStatementWriteOff implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据ID
     */
    private Integer writeOffId;
    /**
     * 签约公司ID
     */
    private Integer orgId;
    /**
     * 账单ID
     */
    private Integer statementId;

    /**
     * 核销单号
     */
    private String writeOffNum;

    /**
     * 收款客户ID
     */
    private Integer coopId;

    /**
     * 核销日期
     */
    private Date writeOffDate;

    /**
     * 核销币种
     */
    private String currency;

    /**
     * 核销金额
     */
    private BigDecimal amountWriteOff;

    /**
     * 核销备注
     */
    private String writeOffRemark;

    /**
     * 创建人ID
     */
    private Integer creatorId;

    /**
     * 创建人名称
     */
    private String creatorName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 凭证日期
     */
    private Date voucherDate;

    /**
     * 凭证号
     */
    private Integer voucherNumber;

    /**
     * 凭证制作人
     */
    private String voucherCreatorId;

    /**
     * 凭证制作人名称
     */
    private String voucherCreatorName;

    /**
     * 凭证制作时间
     */
    private Date voucherCreateTime;

    /**
     * 科目名称
     */
    private String financialAccountName;
    /**
     * 科目代码
     */
    private String financialAccountCode;
    /**
     * 科目类型
     */
    private String financialAccountType;
    /**
     * 数据ID
     */
    private String rowUuid;

}
