package com.efreight.afbase.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.CssCostInvoiceDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * CSS 应付：发票明细表 Mapper 接口
 * </p>
 *
 * @author xiaobo
 * @since 2021-01-04
 */
public interface CssCostInvoiceDetailMapper extends BaseMapper<CssCostInvoiceDetail> {


    @Select("<script>" +
            "SELECT C.payment_num,C.payment_id,CASE WHEN B.writeoff_complete=0 THEN '部分核销' WHEN B.writeoff_complete=1 THEN '核销完毕' WHEN A.invoice_status=1 THEN '收票完毕' WHEN A.invoice_status=0 THEN '部分收票' ELSE '待收票' END AS status," +
            "A.business_scope,A.customer_id,A.customer_name,invoice_detail_id,B.invoice_num,B.invoice_date,B.invoice_type,B.currency,B.amount,B.amount_writeoff,CASE WHEN B.amount IS NULL THEN NULL ELSE B.amount-IFNULL(B.amount_writeoff,0) END AS amountNoWriteoff," +
            "A.invoice_id,A.row_uuid AS invoiceRowUuid,C.row_uuid AS paymentRowUuid,A.creator_name AS invoiceName,A.create_time AS invoiceTime,A.apply_remark,B.creator_name,B.create_time,B.invoice_remark" +
            " FROM css_cost_invoice A" +
            " LEFT JOIN css_cost_invoice_detail B ON A.invoice_id=B.invoice_id" +
            " LEFT JOIN css_payment C ON A.payment_id=C.payment_id" +
            " <when test='cssCostInvoiceDetail.businessScope!=null and (cssCostInvoiceDetail.businessScope==\"AE\" or cssCostInvoiceDetail.businessScope==\"AI\")'>" +
            " LEFT JOIN (select c.payment_id,group_concat(d.order_code) as order_code,group_concat(d.awb_number) as awb_number from (select a.payment_id,a.order_id from css_payment_detail a inner join css_payment b on a.payment_id=b.payment_id where a.org_id=#{cssCostInvoiceDetail.orgId} and b.business_scope=#{cssCostInvoiceDetail.businessScope} group by payment_id,order_id) c inner join af_order d on c.order_id=d.order_id group by c.payment_id ) G on G.payment_id=A.payment_id" +
            "</when>" +
             " <when test='cssCostInvoiceDetail.businessScope!=null and (cssCostInvoiceDetail.businessScope==\"SE\" or cssCostInvoiceDetail.businessScope==\"SI\")'>" +
            " LEFT JOIN (select c.payment_id,group_concat(d.order_code) as order_code,group_concat(d.mbl_number) as awb_number from (select a.payment_id,a.order_id from css_payment_detail a inner join css_payment b on a.payment_id=b.payment_id where a.org_id=#{cssCostInvoiceDetail.orgId} and b.business_scope=#{cssCostInvoiceDetail.businessScope} group by payment_id,order_id) c inner join sc_order d on c.order_id=d.order_id group by c.payment_id ) G on G.payment_id=A.payment_id" +
            "</when>" +
             " <when test='cssCostInvoiceDetail.businessScope!=null and (cssCostInvoiceDetail.businessScope==\"TE\" or cssCostInvoiceDetail.businessScope==\"TI\")'>" +
            " LEFT JOIN (select c.payment_id,group_concat(d.order_code) as order_code,group_concat(d.rwb_number) as awb_number from (select a.payment_id,a.order_id from css_payment_detail a inner join css_payment b on a.payment_id=b.payment_id where a.org_id=#{cssCostInvoiceDetail.orgId} and b.business_scope=#{cssCostInvoiceDetail.businessScope} group by payment_id,order_id) c inner join tc_order d on c.order_id=d.order_id group by c.payment_id ) G on G.payment_id=A.payment_id" +
            "</when>" +
             " <when test='cssCostInvoiceDetail.businessScope!=null and cssCostInvoiceDetail.businessScope==\"LC\"'>" +
            " LEFT JOIN (select c.payment_id,group_concat(d.order_code) as order_code,group_concat(d.customer_number) as awb_number from (select a.payment_id,a.order_id from css_payment_detail a inner join css_payment b on a.payment_id=b.payment_id where a.org_id=#{cssCostInvoiceDetail.orgId} and b.business_scope=#{cssCostInvoiceDetail.businessScope} group by payment_id,order_id) c inner join lc_order d on c.order_id=d.order_id group by c.payment_id ) G on G.payment_id=A.payment_id" +
            "</when>" +
             " <when test='cssCostInvoiceDetail.businessScope!=null and cssCostInvoiceDetail.businessScope==\"IO\"'>" +
            " LEFT JOIN (select c.payment_id,group_concat(d.order_code) as order_code,group_concat(d.customer_number) as awb_number from (select a.payment_id,a.order_id from css_payment_detail a inner join css_payment b on a.payment_id=b.payment_id where a.org_id=#{cssCostInvoiceDetail.orgId} and b.business_scope=#{cssCostInvoiceDetail.businessScope} group by payment_id,order_id) c inner join io_order d on c.order_id=d.order_id group by c.payment_id ) G on G.payment_id=A.payment_id" +
            "</when>" +
            " WHERE A.org_id=#{cssCostInvoiceDetail.orgId}" +
            " <when test='cssCostInvoiceDetail.businessScope!=null and cssCostInvoiceDetail.businessScope!=\"\"'>" +
            " and A.business_scope=#{cssCostInvoiceDetail.businessScope}" +
            "</when>" +
            " <when test='cssCostInvoiceDetail.status!=null and cssCostInvoiceDetail.status!=\"\" and cssCostInvoiceDetail.status==\"待收票\"'>" +
            " and A.invoice_status=-1" +
            "</when>" +
            " <when test='cssCostInvoiceDetail.status!=null and cssCostInvoiceDetail.status!=\"\" and cssCostInvoiceDetail.status==\"部分收票\"'>" +
            " and A.invoice_status=0" +
            "</when>" +
            " <when test='cssCostInvoiceDetail.status!=null and cssCostInvoiceDetail.status!=\"\" and cssCostInvoiceDetail.status==\"收票完毕\"'>" +
            " and A.invoice_status=1 and B.writeoff_complete is null" +
            "</when>" +
            " <when test='cssCostInvoiceDetail.status!=null and cssCostInvoiceDetail.status!=\"\" and cssCostInvoiceDetail.status==\"部分核销\"'>" +
            " and B.writeoff_complete=0" +
            "</when>" +
            " <when test='cssCostInvoiceDetail.status!=null and cssCostInvoiceDetail.status!=\"\" and cssCostInvoiceDetail.status==\"核销完毕\"'>" +
            " and B.writeoff_complete=1" +
            "</when>" +
            " <when test='cssCostInvoiceDetail.status!=null and cssCostInvoiceDetail.status!=\"\" and cssCostInvoiceDetail.status==\"未核销完毕\"'>" +
            " and A.invoice_status!=-1 and (B.writeoff_complete=0 or B.writeoff_complete is null)" +
            "</when>" +
            " <when test='cssCostInvoiceDetail.customerName!=null and cssCostInvoiceDetail.customerName!=\"\"'>" +
            " and A.customer_name like \"%\"#{cssCostInvoiceDetail.customerName}\"%\"" +
            "</when>" +
            " <when test='cssCostInvoiceDetail.invoiceTimeStart!=null'>" +
            " and A.create_time <![CDATA[>=]]> #{cssCostInvoiceDetail.invoiceTimeStart}" +
            "</when>" +
            " <when test='cssCostInvoiceDetail.invoiceTimeEnd!=null'>" +
            " and A.create_time <![CDATA[<=]]> #{cssCostInvoiceDetail.invoiceTimeEnd}" +
            "</when>" +
            " <when test='cssCostInvoiceDetail.createTimeStart!=null'>" +
            " and B.create_time <![CDATA[>=]]> #{cssCostInvoiceDetail.createTimeStart}" +
            "</when>" +
            " <when test='cssCostInvoiceDetail.createTimeEnd!=null'>" +
            " and B.create_time <![CDATA[<=]]> #{cssCostInvoiceDetail.createTimeEnd}" +
            "</when>" +
             " <when test='cssCostInvoiceDetail.invoiceDateStart!=null'>" +
            " and B.invoice_date <![CDATA[>=]]> #{cssCostInvoiceDetail.invoiceDateStart}" +
            "</when>" +
            " <when test='cssCostInvoiceDetail.invoiceDateEnd!=null'>" +
            " and B.invoice_date <![CDATA[<=]]> #{cssCostInvoiceDetail.invoiceDateEnd}" +
            "</when>" +
            " <when test='cssCostInvoiceDetail.invoiceName!=null and cssCostInvoiceDetail.invoiceName!=\"\"'>" +
            " and A.creator_name LIKE \"%\"#{cssCostInvoiceDetail.invoiceName}\"%\"" +
            "</when>" +
            " <when test='cssCostInvoiceDetail.invoiceNum!=null and cssCostInvoiceDetail.invoiceNum!=\"\"'>" +
            " and B.invoice_num LIKE \"%\"#{cssCostInvoiceDetail.invoiceNum}\"%\"" +
            "</when>" +
             " <when test='cssCostInvoiceDetail.creatorName!=null and cssCostInvoiceDetail.creatorName!=\"\"'>" +
            " and B.creator_name LIKE \"%\"#{cssCostInvoiceDetail.creatorName}\"%\"" +
            "</when>" +
            " <when test='cssCostInvoiceDetail.paymentNum!=null and cssCostInvoiceDetail.paymentNum!=\"\"'>" +
            " and C.payment_num LIKE #{cssCostInvoiceDetail.paymentNum}\"%\"" +
            "</when>" +
            " <when test='cssCostInvoiceDetail.invoiceType!=null and cssCostInvoiceDetail.invoiceType!=\"\"'>" +
            " and B.invoice_type = #{cssCostInvoiceDetail.invoiceType}" +
            "</when>" +
            " <when test='cssCostInvoiceDetail.awbNumberOrOrderCode!=null and cssCostInvoiceDetail.awbNumberOrOrderCode!=\"\"'>" +
            " and (G.order_code LIKE \"%\"#{cssCostInvoiceDetail.awbNumberOrOrderCode}\"%\" or G.awb_number LIKE \"%\"#{cssCostInvoiceDetail.awbNumberOrOrderCode}\"%\")" +
            "</when>" +
            "order by A.invoice_id desc" +
            "</script>")
    IPage<CssCostInvoiceDetail> getPage(Page page, @Param("cssCostInvoiceDetail") CssCostInvoiceDetail cssCostInvoiceDetail);
}
