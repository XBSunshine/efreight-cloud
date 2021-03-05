package com.efreight.afbase.dao;

import com.efreight.afbase.entity.CssIncomeInvoiceDetail;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * CSS 应收：发票明细表 Mapper 接口
 * </p>
 *
 * @author cwd
 * @since 2020-12-28
 */
public interface CssIncomeInvoiceDetailMapper extends BaseMapper<CssIncomeInvoiceDetail> {
	
	@Select({"<script>",
        "SELECT A.org_id,A.order_id,CASE",
        " WHEN MIN(IFNULL(B.debit_note_id,0)) <![CDATA[>]]>0 AND MIN(IFNULL(C.writeoff_complete,0)) =1 THEN '核销完毕'",
        " WHEN MAX(C.writeoff_complete) IS NOT NULL THEN '部分核销'",
        " WHEN MAX(m.debit_note_id) is  null AND MAX(m2.debit_note_id) is  null AND ",
        " if(",
        "	     MAX(C.statement_id) IS NOT NULL,",
        "	     if(MIN(D2.invoice_status)=1,if(IFNULL(MIN(D1.invoice_status),1)=1,1,0),0),",
        "	     if(MIN(D1.invoice_status)=1,1,0)",
        "	 )=1 ",
        " AND MIN(IFNULL(B.debit_note_id,0)) <![CDATA[ >]]>0 THEN '完全开票'",
//        " WHEN (MIN(IFNULL(C.statement_id,0))<![CDATA[ >]]>0 OR  MIN(IFNULL(D1.invoice_status,0)) = 1) AND (MIN(IFNULL(C.statement_id,0)) =0 OR MIN(IFNULL(D2.invoice_status,0)) =1) AND MIN(IFNULL(B.debit_note_id,0)) <![CDATA[ >]]>0 THEN '完全开票'",
        " WHEN MAX(D1.invoice_status) <![CDATA[ > ]]>-1 OR MAX(D2.invoice_status) <![CDATA[ > ]]>-1 THEN '部分开票'",
        " WHEN MAX(D1.invoice_status) =-1 OR MAX(D2.invoice_status) = -1 THEN '开票申请'",
        " WHEN MAX(C.statement_id) IS NOT NULL THEN '已制清单'",
        " WHEN MAX(B.debit_note_id) IS NOT NULL THEN '已制账单'",
        " WHEN MAX(B.order_id) IS NOT NULL THEN '已录收入'",
        " WHEN MAX(B.order_id) IS NULL THEN '未录收入'",
        " ELSE '' END AS income_status",
        " FROM",
        "  <when test='businessScope==\"AE\" or businessScope==\"AI\"'>",
        " af_order A",
        " LEFT JOIN af_income B ON A.order_id=B.order_id",
        "  </when>",
        "  <when test='businessScope==\"SE\" or businessScope==\"SI\"'>",
        " sc_order A",
        " LEFT JOIN sc_income B ON A.order_id=B.order_id",
        "  </when>",
        "  <when test='businessScope==\"TE\" or businessScope==\"TI\"'>",
        " tc_order A",
        " LEFT JOIN tc_income B ON A.order_id=B.order_id",
        "  </when>",
        "  <when test='businessScope==\"LC\"'>",
        " lc_order A",
        " LEFT JOIN lc_income B ON A.order_id=B.order_id",
        "  </when>",
        "  <when test='businessScope==\"IO\"'>",
        " io_order A",
        " LEFT JOIN io_income B ON A.order_id=B.order_id",
        "  </when>",
        " LEFT JOIN css_debit_note C ON A.order_id=C.order_id AND C.business_scope =#{businessScope}",
        " LEFT JOIN css_income_invoice D1 ON C.debit_note_id = D1.debit_note_id",
        " LEFT JOIN css_income_invoice D2 ON C.statement_id  = D2.statement_id",
        " left join (",
        "	    select c2.debit_note_id,c2.order_id,c2.org_id from css_debit_note c2 ",
        "	    left join css_income_invoice d3 ON c2.debit_note_id = d3.debit_note_id",   
        "	    where c2.org_id=#{orgId} and c2.order_id in (${orderId}) and c2.business_scope=#{businessScope} and c2.statement_id is null  and d3.invoice_id is null", 
        "	   ) m  on m.debit_note_id = C.debit_note_id",
        " left join (",
        "	    select c3.debit_note_id,c3.order_id,c3.org_id from css_debit_note c3 ",
        "	    left join css_income_invoice d4 ON c3.statement_id = d4.statement_id",   
        "	    where c3.org_id=#{orgId} and c3.order_id in (${orderId}) and c3.business_scope=#{businessScope} and c3.statement_id is not null  and d4.invoice_id is null", 
        "	   ) m2  on m2.debit_note_id = C.debit_note_id",
        " WHERE",
        " A.org_id=#{orgId}",
        " AND A.order_id in (${orderId})",
        " AND A.business_scope =#{businessScope}",
        " GROUP BY A.org_id,A.order_id",
        "</script>"})
  List<Map> getOrderIncomeStatus(@Param("orgId") Integer orgId, @Param("orderId") String orderId,@Param("businessScope") String businessScope);
	
	@Update({"<script>",
		    "update ",
		    "  <when test='businessScope==\"AE\" or businessScope==\"AI\"'>",
	        " af_order ",
	        "  </when>",
	        "  <when test='businessScope==\"SE\" or businessScope==\"SI\"'>",
	        " sc_order",
	        "  </when>",
	        "  <when test='businessScope==\"TE\" or businessScope==\"TI\"'>",
	        " tc_order",
	        "  </when>",
	        "  <when test='businessScope==\"LC\"'>",
	        " lc_order ",
	        "  </when>",
	        "  <when test='businessScope==\"IO\"'>",
	        " io_order ",
	        "  </when>",
	        " set ",
         " income_status=#{incomeStatus},row_uuid=#{rowUuid}",
         " where  org_id = #{orgId} and order_id = #{orderId} ",
	"</script>"})
    void updateOrderIncomeStatus(@Param("orgId") Integer orgId, @Param("orderId") Integer orderId, @Param("incomeStatus") String incomeStatus, @Param("rowUuid") String rowUuid, @Param("businessScope") String businessScope);

}
