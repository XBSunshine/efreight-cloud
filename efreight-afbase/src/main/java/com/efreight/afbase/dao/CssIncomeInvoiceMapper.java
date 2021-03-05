package com.efreight.afbase.dao;

import com.efreight.afbase.entity.CssIncomeInvoice;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * <p>
 * CSS 应收：发票申请表 Mapper 接口
 * </p>
 *
 * @author cwd
 * @since 2020-12-28
 */
public interface CssIncomeInvoiceMapper extends BaseMapper<CssIncomeInvoice> {
	@Select({"<script>",
		"select a.debit_note_id,a.statement_id,a.invoice_status,a.customer_name,a.creator_name,a.create_time,a.apply_remark,",
		" a.taxpayer_num,a.address,a.phone_number,a.bank_name,a.bank_number,a.row_uuid,a.invoice_id,a.customer_id,a.business_scope,a.invoice_title AS open_invoice_title,a.invoice_type AS open_invoice_type,",
		"IFNULL(c.debit_note_num,d.statement_num) AS business_num ,IFNULL(c.row_uuid,d.row_uuid) AS business_row_uuid,",
		"b.invoice_detail_id,b.invoice_title,b.invoice_num,b.invoice_type,b.amount,b.currency,b.amount_writeoff,(IFNULL(b.amount,0)-IFNULL(b.amount_writeoff,0)) AS amount_writeoff_no",
		",b.create_time AS open_invoice_time,b.creator_name AS open_invoice_user_name,b.invoice_remark AS open_invoice_remark,b.writeoff_complete ",
		" from ",
		" css_income_invoice a",
		"<when test='bean.orderAwbNumber!=null and bean.orderAwbNumber!=\"\"'>",
        " inner join ",
        " (SELECT e.debit_note_id,e.statement_id FROM css_debit_note e ",
        "  <when test='bean.businessScope==\"AE\" or bean.businessScope==\"AI\"'>",
        "  INNER JOIN af_order f ON e.order_id = f.order_id",
        " where f.business_scope=#{bean.businessScope} ",
        " and (f.order_code like \"%\"#{bean.orderAwbNumber}\"%\" or f.awb_number like \"%\"#{bean.orderAwbNumber}\"%\")",
        "  </when>",
        "  <when test='bean.businessScope==\"SE\" or bean.businessScope==\"SI\"'>",
        "  INNER JOIN sc_order f ON e.order_id = f.order_id",
        " where f.business_scope=#{bean.businessScope} ",
        " and (f.order_code like \"%\"#{bean.orderAwbNumber}\"%\" or f.mbl_number like \"%\"#{bean.orderAwbNumber}\"%\")",
        "  </when>",
        "  <when test='bean.businessScope==\"TE\" or bean.businessScope==\"TI\"'>",
        "  INNER JOIN tc_order f ON e.order_id = f.order_id",
        " where f.business_scope=#{bean.businessScope} ",
        " and (f.order_code like \"%\"#{bean.orderAwbNumber}\"%\" or f.rwb_number like \"%\"#{bean.orderAwbNumber}\"%\")",
        "  </when>",
        "  <when test='bean.businessScope==\"LC\"'>",
        "  INNER JOIN lc_order f ON e.order_id = f.order_id",
        " where f.business_scope=#{bean.businessScope} ",
        " and f.order_code like \"%\"#{bean.orderAwbNumber}\"%\" ",
        "  </when>",
        "  <when test='bean.businessScope==\"IO\"'>",
        "  INNER JOIN io_order f ON e.order_id = f.order_id",
        " where f.business_scope=#{bean.businessScope} ",
        " and f.order_code like \"%\"#{bean.orderAwbNumber}\"%\" ",
        "  </when>",
        "  and e.org_id=#{bean.orgId}",
        " ) as m on IFNULL(a.debit_note_id,a.statement_id) = IFNULL(m.statement_id,m.debit_note_id) ",
        "</when>",
		" LEFT JOIN css_income_invoice_detail  b on a.invoice_id=b.invoice_id",
		" LEFT JOIN css_debit_note c on a.debit_note_id=c.debit_note_id",
		" LEFT JOIN css_statement d on a.statement_id=d.statement_id",
		" where 1=1",
		" and a.org_id=#{bean.orgId} and a.business_scope=#{bean.businessScope}",
		"<when test='bean.InvoiceStatusStr!=null and bean.InvoiceStatusStr!=\"\" and bean.InvoiceStatusStr==\"待开票\"'>",
        " AND a.invoice_status=-1",
        "</when>",
        "<when test='bean.InvoiceStatusStr!=null and bean.InvoiceStatusStr!=\"\" and bean.InvoiceStatusStr==\"部分开票\"'>",
        " AND a.invoice_status=0",
        "</when>",
        "<when test='bean.InvoiceStatusStr!=null and bean.InvoiceStatusStr!=\"\" and bean.InvoiceStatusStr==\"开票完毕\"'>",
        " AND a.invoice_status=1",
        "</when>",
        "<when test='bean.InvoiceStatusStr!=null and bean.InvoiceStatusStr!=\"\" and bean.InvoiceStatusStr==\"部分核销\"'>",
        " AND b.writeoff_complete=0",
        "</when>",
        "<when test='bean.InvoiceStatusStr!=null and bean.InvoiceStatusStr!=\"\" and bean.InvoiceStatusStr==\"核销完毕\"'>",
        " AND b.writeoff_complete=1",
        "</when>",
        "<when test='bean.InvoiceStatusStr!=null and bean.InvoiceStatusStr!=\"\" and bean.InvoiceStatusStr==\"未核销完毕\"'>",
        " AND IFNULL(b.writeoff_complete,0)!= 1 and a.invoice_status!=-1",
        "</when>",
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
        "<when test='bean.businessNum!=null and bean.businessNum!=\"\"'>",
        " AND ((c.debit_note_num is not null and c.debit_note_num like \"%\"#{bean.businessNum}\"%\")",
        "  or (d.statement_num is not null and d.statement_num like \"%\"#{bean.businessNum}\"%\"))",
        "</when>",
        "<when test='bean.openInvoiceTimeStart!=null and bean.openInvoiceTimeStart!=\"\"'>",
        " AND b.create_time  <![CDATA[ >= ]]> #{bean.openInvoiceTimeStart} ",
        "</when>",
        "<when test='bean.openInvoiceTimeEnd!=null and bean.openInvoiceTimeEnd!=\"\"'>",
        " AND b.create_time  <![CDATA[ <= ]]> #{bean.openInvoiceTimeEnd} ",
        "</when>",
        "<when test='bean.openInvoiceUserName!=null and bean.openInvoiceUserName!=\"\"'>",
        " AND b.creator_name like \"%\"#{bean.openInvoiceUserName}\"%\"",
        "</when>",
        "<when test='bean.invoiceType!=null and bean.invoiceType!=\"\"'>",
        " AND b.invoice_type=#{bean.invoiceType} ",
        "</when>",
        "<when test='bean.invoiceDateStart!=null and bean.invoiceDateStart!=\"\"'>",
        " AND b.invoice_date  <![CDATA[ >= ]]> #{bean.invoiceDateStart} ",
        "</when>",
        "<when test='bean.invoiceDateEnd!=null and bean.invoiceDateEnd!=\"\"'>",
        " AND b.invoice_date  <![CDATA[ <= ]]> #{bean.invoiceDateEnd} ",
        "</when>",
        " order by a.invoice_id desc ",
	 "</script>"})
   IPage<CssIncomeInvoice> getPage(Page page, @Param("bean") CssIncomeInvoice bean);

}
