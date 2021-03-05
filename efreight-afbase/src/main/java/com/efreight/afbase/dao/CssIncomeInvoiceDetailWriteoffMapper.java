package com.efreight.afbase.dao;

import com.efreight.afbase.entity.AfIncome;
import com.efreight.afbase.entity.CssIncomeInvoice;
import com.efreight.afbase.entity.CssIncomeInvoiceDetailWriteoff;
import com.efreight.afbase.entity.CssIncomeWriteoff;

import java.math.BigDecimal;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * <p>
 * CSS 应收：发票明细 核销表 Mapper 接口
 * </p>
 *
 * @author cwd
 * @since 2020-12-28
 */
public interface CssIncomeInvoiceDetailWriteoffMapper extends BaseMapper<CssIncomeInvoiceDetailWriteoff> {

	 @Select({"<script>",
         "select * from css_income_invoice_detail_writeoff",
         "	where org_id = #{org_id} and writeoff_num like  \"%\"#{writeoff_num}\"%\"",
         " ORDER BY invoice_detail_writeoff_id DESC ",
         "</script>"})
    List<CssIncomeInvoiceDetailWriteoff> selectCode(@Param("org_id") Integer org_id, @Param("writeoff_num") String writeoff_num);
	 
	 @Update({"<script>",
		    "update css_debit_note_currency",
	        " set amount_writeoff=null",
      " where  org_id = #{orgId} and debit_note_id = #{debitNoteId} and currency=#{currency}",
	"</script>"})
     void updateDebitNoteAmountWriteoff(@Param("orgId") Integer orgId,@Param("debitNoteId") Integer debitNoteId,@Param("currency") String currency);
	 @Update({"<script>",
		    "update css_statement_currency",
	        " set amount_writeoff=null,functional_amount_writeoff=null",
   " where  org_id = #{orgId} and statement_id = #{statementId} and currency=#{currency}",
	"</script>"})
  void updateStatementAmountWriteoff(@Param("orgId") Integer orgId,@Param("statementId") Integer statementId,@Param("currency") String currency);
	 
	 @Update({"<script>",
		    "update ",
		    "  <when test='businessScope==\"AE\" or businessScope==\"AI\"'>",
	        " af_income ",
	        "  </when>",
	        "  <when test='businessScope==\"SE\" or businessScope==\"SI\"'>",
	        " sc_income",
	        "  </when>",
	        "  <when test='businessScope==\"TE\" or businessScope==\"TI\"'>",
	        " tc_income",
	        "  </when>",
	        "  <when test='businessScope==\"LC\"'>",
	        " lc_income ",
	        "  </when>",
	        "  <when test='businessScope==\"IO\"'>",
	        " io_income ",
	        "  </when>",
	        " set income_amount_writeoff=null",
            " where  org_id = #{orgId} and debit_note_id = #{debitNoteId} and income_currency=#{currency}",
	"</script>"})
    void updateIncomeAmountWriteoff(@Param("orgId") Integer orgId,@Param("debitNoteId") Integer debitNoteId,@Param("currency") String currency,@Param("businessScope") String businessScope);
	 
	 @Select({"<script>",
		    "select * from ",
		    "  <when test='businessScope==\"AE\" or businessScope==\"AI\"'>",
	        " af_income ",
	        "  </when>",
	        "  <when test='businessScope==\"SE\" or businessScope==\"SI\"'>",
	        " sc_income",
	        "  </when>",
	        "  <when test='businessScope==\"TE\" or businessScope==\"TI\"'>",
	        " tc_income",
	        "  </when>",
	        "  <when test='businessScope==\"LC\"'>",
	        " lc_income ",
	        "  </when>",
	        "  <when test='businessScope==\"IO\"'>",
	        " io_income ",
	        "  </when>",
            " where  org_id = #{orgId} and debit_note_id = #{debitNoteId} and income_currency=#{currency}",
            " order by income_amount asc",
	"</script>"})
    List<AfIncome> queryIncomeAmountWriteoff(@Param("orgId") Integer orgId,@Param("debitNoteId") Integer debitNoteId,@Param("currency") String currency,@Param("businessScope") String businessScope);	 
	 
	 
	 @Update({"<script>",
		    "update ",
		    "  <when test='businessScope==\"AE\" or businessScope==\"AI\"'>",
	        " af_income ",
	        "  </when>",
	        "  <when test='businessScope==\"SE\" or businessScope==\"SI\"'>",
	        " sc_income",
	        "  </when>",
	        "  <when test='businessScope==\"TE\" or businessScope==\"TI\"'>",
	        " tc_income",
	        "  </when>",
	        "  <when test='businessScope==\"LC\"'>",
	        " lc_income ",
	        "  </when>",
	        "  <when test='businessScope==\"IO\"'>",
	        " io_income ",
	        "  </when>",
	        " set income_amount_writeoff=#{incomeAmountWriteoff},row_uuid=#{rowUuid}",
         " where  income_id = #{incomeId}",
	"</script>"})
 void updateIncomeAmountWriteoffTwo(@Param("incomeId") Integer incomeId,@Param("incomeAmountWriteoff") BigDecimal incomeAmountWriteoff,@Param("rowUuid") String rowUuid,@Param("businessScope") String businessScope);
	 
		@Select({"<script>",
			"select a.*,b.invoice_num,b.amount,b.invoice_date,  ",
			"IFNULL(c.debit_note_num,d.statement_num) AS business_num ",
			" from ",
			" css_income_invoice_detail_writeoff a ",
			" left join css_income_invoice_detail b on a.invoice_detail_id=b.invoice_detail_id",
			" left join css_debit_note c on a.debit_note_id=c.debit_note_id ",
			" left join css_statement d on a.statement_id = d.statement_id",
			" where a.org_id=#{bean.orgId} ",
			" <when test='bean.businessNum!=null and bean.businessNum!=\"\"'>",
	        " and (d.statement_num like \"%\"#{bean.businessNum}\"%\" or c.debit_note_num like \"%\"#{bean.businessNum}\"%\")",
	        " </when>",
	        "  <when test='bean.businessScope!=null and bean.businessScope!=\"\"'>",
	        "  and a.business_scope=#{bean.businessScope}",
	        "  </when>",
	        "<when test='bean.customerName!=null and bean.customerName!=\"\"'>",
	        " AND a.customer_name like \"%\"#{bean.customerName}\"%\"",
	        "</when>",
	        "<when test='bean.createTimeStart!=null and bean.createTimeStart!=\"\"'>",
	        " AND a.create_time  <![CDATA[ >= ]]> #{bean.createTimeStart} ",
	        "</when>",
	        "<when test='bean.createTimeEnd!=null and bean.createTimeEnd!=\"\"'>",
	        " AND a.create_time  <![CDATA[ <= ]]> #{bean.createTimeEnd} ",
	        "</when>",
	        "<when test='bean.creatorName!=null and bean.creatorName!=\"\"'>",
	        " AND a.creator_name like \"%\"#{bean.creatorName}\"%\"",
	        "</when>",
	        "<when test='bean.invoiceNum!=null and bean.invoiceNum!=\"\"'>",
	        " AND b.invoice_num like \"%\"#{bean.invoiceNum}\"%\"",
	        "</when>",
	        "<when test='bean.writeoffNum!=null and bean.writeoffNum!=\"\"'>",
	        " AND a.writeoff_num like \"%\"#{bean.writeoffNum}\"%\"",
	        "</when>",
	        "<when test='bean.writeoffDateStart!=null and bean.writeoffDateStart!=\"\"'>",
	        " AND a.writeoff_date  <![CDATA[ >= ]]> #{bean.writeoffDateStart} ",
	        "</when>",
	        "<when test='bean.writeoffDateEnd!=null and bean.writeoffDateEnd!=\"\"'>",
	        " AND a.writeoff_date  <![CDATA[ <= ]]> #{bean.writeoffDateEnd} ",
	        "</when>",
	        "<when test='bean.invoiceDateStart!=null and bean.invoiceDateStart!=\"\"'>",
	        " AND b.invoice_date  <![CDATA[ >= ]]> #{bean.invoiceDateStart} ",
	        "</when>",
	        "<when test='bean.invoiceDateEnd!=null and bean.invoiceDateEnd!=\"\"'>",
	        " AND b.invoice_date  <![CDATA[ <= ]]> #{bean.invoiceDateEnd} ",
	        "</when>",
	        " order by a.invoice_detail_writeoff_id desc ",
		 "</script>"})
	   IPage<CssIncomeInvoiceDetailWriteoff> getPage(Page page, @Param("bean") CssIncomeInvoiceDetailWriteoff bean);	 
}
