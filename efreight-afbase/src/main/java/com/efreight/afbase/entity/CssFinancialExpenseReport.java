package com.efreight.afbase.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;

import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.util.List;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * CSS 财务费用报销
 * </p>
 *
 * @author caiwd
 * @since 2020-10-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("css_financial_expense_report")
public class CssFinancialExpenseReport implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 报销单ID
     */
    @TableId(value = "expense_report_id", type = IdType.AUTO)
    private Integer expenseReportId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 报销单号
     */
    private String expenseReportNum;

    /**
     * 报销单状态：已暂存、已提交、已审批、已审核、已付款、已退回
     */
    private String expenseReportStatus;

    /**
     * 报销单：日期
     */
    private LocalDate expenseReportDate;

    /**
     * 申请人：部门
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer deptId;

    /**
     * 申请人：部门负责人
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer deptManagerId;

    /**
     * 付款方式：现金、支票、转账 
     */
    private String paymentMethod;

    /**
     * 报销性质：对公、个人
     */
    private String expenseReportMode;

    /**
     * 费用用途
     */
    private String expensesUse;

    /**
     * 报销金额：小写
     */
    private BigDecimal expenseAmount;

    /**
     * 报销备注
     */
    private String expenseReportRemark;

    /**
     * 审批人 ID
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer approvalDeptManagerId;

    private String approvalDeptManagerName;

    /**
     * 审批时间
     */
    private LocalDateTime approvalDeptManagerTime;

    /**
     * 审核人 ID
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer approvalFinancialUserId;

    private String approvalFinancialUserName;

    /**
     * 审核时间
     */
    private LocalDateTime approvalFinancialTime;

    /**
     * 付款人ID
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer payerId;

    private String payerName;

    /**
     * 付款时间
     */
    private LocalDateTime payerTime;
    /**
     * 财务月度
     */
    private LocalDate financialDate;

    /**
     * 费用科目名称
     */
    private String expenseFinancialAccountName;

    /**
     * 费用科目代码
     */
    private String expenseFinancialAccountCode;

    /**
     * 银行科目名称
     */
    private String bankFinancialAccountName;

    /**
     * 银行科目代码
     */
    private String bankFinancialAccountCode;

    /**
     * 财务：凭证日期
     */
    private LocalDate voucherDate;

    /**
     * 财务：凭证号
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer voucherNumber;

    /**
     * 财务：凭证制作人
     */
    @TableField(strategy = FieldStrategy.IGNORED)
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
    private LocalDateTime createTime;

    /**
     * 修改人ID
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer editorId;

    /**
     * 修改人名称
     */
    private String editorName;
    @TableField(strategy = FieldStrategy.IGNORED)
    private  Integer attachedDocument;

    /**
     * 修改时间
     */
    private LocalDateTime editTime;
    @TableField(exist = false)
    private String deptName;
    @TableField(exist = false)
    private String expenseReportDateStart;
    @TableField(exist = false)
    private String expenseReportDateEnd;
    @TableField(exist = false)
    private Integer userId;
    @TableField(exist = false)
    private String expenseReportIdStr;
    
   /**
    * 打印
    */
    @TableField(exist = false)
    private Integer year;
    @TableField(exist = false)
    private Integer month;
    @TableField(exist = false)
    private Integer day;
    @TableField(exist = false)
    private String createTimeM;
    @TableField(exist = false)
    private String paymentMethodOne;
    @TableField(exist = false)
    private String paymentMethodTwo;
    @TableField(exist = false)
    private String paymentMethodThree;
    @TableField(exist = false)
    private String expenseReportModeOne;
    @TableField(exist = false)
    private String expenseReportModeTwo;
    @TableField(exist = false)
    private String expenseAmountMax;
    @TableField(exist = false)
    private String approvalDeptManagerTimeStr;
    @TableField(exist = false)
    private String approvalFinancialTimeStr;
    @TableField(exist = false)
    private String expenseFinancialAccount;
    @TableField(exist = false)
    private String bankFinancialAccount;
    @TableField(exist = false)
    private String accounting;
    @TableField(exist = false)
    private String cashier;
    @TableField(exist = false)
    private String accountingTimeStr;
    @TableField(exist = false)
    private String cashierTimeStr;
    @TableField(exist = false)
    private String timeStr;
    @TableField(exist = false)
    private String orgName;
    /**
     * 打印结束
     */
    @TableField(exist = false)
    private String auditType;
    
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer expenseFinancialAccountId;
    private String expenseFinancialAccountType;
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer bankFinancialAccountId;
    private String bankFinancialAccountType;
    @TableField(exist = false)
    private String columnStrs;
    @TableField(exist = false)
    private String fileStrs;
    @TableField(exist = false)
    private List<CssFinancialExpenseReportFiles> listFiles;
    @TableField(exist = false)
    private String code;
}
