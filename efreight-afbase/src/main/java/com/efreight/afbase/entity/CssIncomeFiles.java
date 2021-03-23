package com.efreight.afbase.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("css_income_files")
public class CssIncomeFiles implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	 /**
     * 文件IDI
     */
    @TableId(value = "file_id", type = IdType.AUTO)
    private Integer fileId;
    
    private Integer orgId;
    
    private String businessScope;
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer debitNoteId;
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer statementId;
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer invoiceDetailId;
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer invoiceDetailWriteoffId;
    
    private String fileType;
    
    private String fileName;
    
    private String fileUrl;
    
    private String fileRemark;
    
    private Integer creatorId;
    
    private String creatorName;
    
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String fileStrs;
    @TableField(exist = false)
    private String businessType;
}
