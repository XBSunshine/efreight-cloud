package com.efreight.prm.entity.writeoff;

import lombok.Data;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author lc
 * @date 2021/3/15 8:53
 */
@Data
public class WriteOffConfirm implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据ID
     */
    private String rowId;
    /**
     * 账单ID
     */
    private Integer statementId;
    /**
     * 核销日期
     */
    private Date date;
    /**
     * 核销金额
     */
    private BigDecimal amount ;
    /**
     * 备注信息
     */
    private String remark;

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


    public void check(){
        Assert.hasLength(this.rowId, "数据ID为空,核销失败");
        Assert.notNull(this.statementId, "数据ID为空,核销失败");
        Assert.notNull(this.amount, "请输入核销金额");
        Assert.notNull(this.date, "请输入核销日期");
        Assert.hasLength(this.financialAccountCode, "请选择科目信息");
        Assert.isTrue(BigDecimal.ZERO.compareTo(amount) <= 0, "核销金额不能小于0");
    }
}
