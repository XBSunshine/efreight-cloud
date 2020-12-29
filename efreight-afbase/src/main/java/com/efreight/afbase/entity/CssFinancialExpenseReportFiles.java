package com.efreight.afbase.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * CSS 财务费用报销附件
 * </p>
 *
 * @author caiwd
 * @since 2020-10-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("css_financial_expense_report_files")
public class CssFinancialExpenseReportFiles implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	  /**
     * 附件ID
     */
    @TableId(value = "expense_report_file_id", type = IdType.AUTO)
    private Integer expenseReportFileId;
    
    private Integer expenseReportId;
    
    private Integer orgId;
    
    private String fileType;
    
    private String fileName;
    
    private String fileUrl;
    
    private String fileRemark;
	
}
