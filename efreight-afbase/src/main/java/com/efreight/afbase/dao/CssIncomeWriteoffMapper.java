package com.efreight.afbase.dao;

import java.math.BigDecimal;
import java.util.List;

import com.efreight.afbase.entity.*;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * <p>
 * CSS 应收：核销 Mapper 接口
 * </p>
 *
 * @author xiaobo
 * @since 2020-02-03
 */
public interface CssIncomeWriteoffMapper extends BaseMapper<CssIncomeWriteoff> {

    @Update({"<script>",
            " update css_statement",
            " set writeoff_complete=#{writeoff_complete} ",
            "where org_id=#{org_id} and statement_id=#{statement_id}",
            "</script>"})
    void updateListStatus(@Param("org_id") Integer org_id, @Param("statement_id") Integer statement_id,
                          @Param("writeoff_complete") Integer writeoff_complete);

    @Update({"<script>",
            " update css_debit_note",
            " set writeoff_complete=#{writeoff_complete} ",
            "where org_id=#{org_id} and debit_note_id=#{debit_note_id}",
            "</script>"})
    void updateBillStatus(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id,
                          @Param("writeoff_complete") Integer writeoff_complete);

    @Update({"<script>",
            " update css_debit_note",
            " set functional_amount_writeoff=#{functional_amount_writeoff} ,row_uuid=#{row_uuid}",
            "where org_id=#{org_id} and debit_note_id=#{debit_note_id}",
            "</script>"})
    void updateCssDebitNote(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id,
                            @Param("functional_amount_writeoff") BigDecimal functional_amount_writeoff, @Param("row_uuid") String row_uuid);

    @Update({"<script>",
            " update css_statement",
            " set functional_amount_writeoff=#{functional_amount_writeoff} ",
            "where org_id=#{org_id} and statement_id=#{statement_id}",
            "</script>"})
    void updateCssStatement(@Param("org_id") Integer org_id, @Param("statement_id") Integer statement_id,
                            @Param("functional_amount_writeoff") BigDecimal functional_amount_writeoff);

    @Update({"<script>",
            " update css_debit_note_currency",
            " set amount_writeoff=#{amount_writeoff} ,",
            " functional_amount_writeoff=#{functional_amount_writeoff} ",
            "where org_id=#{org_id} and debit_note_id=#{debit_note_id} and currency=#{currency}",
            "</script>"})
    void updateCssDebitNoteCurrency(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id, @Param("currency") String currency,
                                    @Param("amount_writeoff") BigDecimal amount_writeoff,
                                    @Param("functional_amount_writeoff") BigDecimal functional_amount_writeoff);

    @Update({"<script>",
            " update css_statement_currency",
            " set amount_writeoff=#{amount_writeoff} ,",
            " functional_amount_writeoff=#{functional_amount_writeoff} ",
            "where org_id=#{org_id} and statement_id=#{statement_id} and currency=#{currency}",
            "</script>"})
    void updateCssStatementCurrency(@Param("org_id") Integer org_id, @Param("statement_id") Integer statement_id, @Param("currency") String currency,
                                    @Param("amount_writeoff") BigDecimal amount_writeoff,
                                    @Param("functional_amount_writeoff") BigDecimal functional_amount_writeoff);

    @Select({"<script>",
            "SELECT IFNULL(functional_amount_writeoff,0) functional_amount_writeoff ,row_uuid rowUuid",
            " FROM css_debit_note",
            "where org_id=#{org_id} and debit_note_id=#{debit_note_id}",
            "</script>"})
    CssDebitNote getCssDebitNote(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id);

    @Select({"<script>",
            "SELECT IFNULL(functional_amount_writeoff,0) functional_amount_writeoff ,row_uuid rowUuid",
            " FROM css_statement",
            "where org_id=#{org_id} and statement_id=#{statement_id}",
            "</script>"})
    Statement getCssStatement(@Param("org_id") Integer org_id, @Param("statement_id") Integer statement_id);

    @Select({"<script>",
            "SELECT IFNULL(amount_writeoff,0) amount_writeoff,IFNULL(functional_amount,0) functional_amount,IFNULL(amount,0) amount,",
            "IFNULL(functional_amount_writeoff,0) functional_amount_writeoff FROM css_debit_note_currency",
            "where org_id=#{org_id} and debit_note_id=#{debit_note_id} and currency=#{currency}",
            "</script>"})
    CssDebitNoteCurrency getCssDebitNoteCurrency(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id, @Param("currency") String currency);

    @Select({"<script>",
            "SELECT IFNULL(amount_writeoff,0) amount_writeoff,IFNULL(functional_amount,0) functional_amount,IFNULL(amount,0) amount,",
            "IFNULL(functional_amount_writeoff,0) functional_amount_writeoff FROM css_statement_currency",
            "where org_id=#{org_id} and statement_id=#{statement_id} and currency=#{currency}",
            "</script>"})
    StatementCurrency getCssStatementCurrency(@Param("org_id") Integer org_id, @Param("statement_id") Integer statement_id, @Param("currency") String currency);

    @Select({"<script>",
            "SELECT *,amount - IFNULL(amount_writeoff,0) amountWriteoffNo,",
            "functional_amount - IFNULL(functional_amount_writeoff,0) functionalAmountWriteoffNo FROM css_debit_note_currency",
            "where org_id=#{org_id} and debit_note_id=#{debit_note_id}",
            "</script>"})
    List<CssDebitNoteCurrency> queryBillCurrency(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id);

    @Select({"<script>",
            "SELECT\n" +
                    "\tA.financial_account_name AS financialAccountName,\n" +
                    "\tMAX( A.financial_account_id ) AS financialAccountId,\n" +
                    "CASE\n" +
                    "\t\tMAX( A.manage_mode ) \n" +
                    "\t\tWHEN '子科目' THEN\n" +
                    "\t\tIFNULL( A.financial_account_code, '' ) \n" +
                    "\t\tWHEN '辅助账' THEN\n" +
                    "\tCASE\n" +
                    "\t\t\t\n" +
                    "\t\t\tWHEN MAX( A.subsidiary_account ) = '往来单位' THEN\n" +
                    "\t\t\tIFNULL( A.financial_account_code, '' ) ELSE A.financial_account_code \n" +
                    "\t\tEND ELSE A.financial_account_code \n" +
                    "\tEND AS financialAccountCode,\n" +
                    "CASE \t\n" +
                    "    WHEN MAX( A.financial_account_type ) IN ('现金','银行存款') THEN\n" +
                    "\t\t\t'' ELSE \n" +
                    "\t\tCASE\n" +
                    "\t\tMAX( A.manage_mode ) \n" +
                    "\t\tWHEN '子科目' THEN\n" +
                    "\t\t'子科目' \n" +
                    "\t\tWHEN '辅助账' THEN\n" +
                    "\tCASE\n" +
                    "\t\t\t\n" +
                    "\t\t\tWHEN MAX( A.subsidiary_account ) = '往来单位' THEN\n" +
                    "\t\t\t'往来单位' ELSE '' \n" +
                    "\t\tEND ELSE '' \n" +
                    "\tEND\t\n" +
                    "\t\tEND\n" +
                    " AS financialAccountType \n" +
                    "FROM\n" +
                    "\tcss_financial_account A\n" +
                    "\tINNER JOIN prm_coop B ON B.coop_id = #{customerId} \n" +
                    "\tAND A.org_id = B.org_id\n" +
                    "\tLEFT JOIN ( SELECT parent_id, COUNT(*) AS FCOUNT FROM css_financial_account WHERE org_id = #{org_id} AND business_scope = #{businessScope} \n" +
                    "\tAND is_valid = 1 \n" +
                    "\tAND financial_account_class_02 = 1 GROUP BY parent_id ) AS C ON A.financial_account_id = C.parent_id \n" +
                    "WHERE\n" +
                    "\tA.org_id = #{org_id}\n" +
                    "\tAND A.business_scope = #{businessScope} \n" +
                    "\tAND A.is_valid = 1 \n" +
                    "\tAND A.financial_account_class_02 = 1\n" +
                    "GROUP BY\n" +
                    "\tA.financial_account_name,\n" +
                    "\tA.financial_account_code \n" +
                    "HAVING \tMAX( C.FCOUNT ) IS NULL\n" +
                    "ORDER BY\n" +
                    "\tA.financial_account_code ASC",
            "</script>"})
    List<FinancialAccount> getFinancialAccount(@Param("org_id") Integer org_id,@Param("businessScope") String businessScope,@Param("customerId") Integer customerId);

    @Select({"<script>",
            "SELECT\n" +
                    "\tfinancial_code AS financialCode \n" +
                    "FROM\n" +
                    "\tprm_coop \n" +
                    "WHERE\n" +
                    "\tcoop_id = #{customerId}",
            "</script>"})
    String getFinancialCodeByCoopId(@Param("customerId") Integer customerId);

    @Select({"<script>",
            "SELECT *,amount - IFNULL(amount_writeoff,0) amountWriteoffNo,",
            "functional_amount - IFNULL(functional_amount_writeoff,0) functionalAmountWriteoffNo FROM css_statement_currency",
            "where org_id=#{org_id} and statement_id=#{statement_id}",
            "</script>"})
    List<StatementCurrency> queryListCurrency(@Param("org_id") Integer org_id, @Param("statement_id") Integer statement_id);

    @Select({"<script>",
            "select * from css_income_writeoff\n",
            "	where org_id = #{org_id} and writeoff_num like  \"%\"#{writeoff_num}\"%\"",
            " ORDER BY income_writeoff_id DESC ",
            "</script>"})
    List<CssIncomeWriteoff> selectCode(@Param("org_id") Integer org_id, @Param("writeoff_num") String writeoff_num);


    @Select({"<script>",
            "select * from (",
            "    SELECT a.* ",
            " ,b.debit_note_num debitNoteNumStatementNum",
            " ,b.functional_amount",
            " ,d.functional_amount_writeoff",
            " FROM css_income_writeoff a",
            " LEFT JOIN  css_debit_note b ON a.debit_note_id=b.debit_note_id",
            "<when test='bean.businessScope==\"AI\" or bean.businessScope==\"AE\"'>",
            " LEFT JOIN  af_order ba ON b.order_id=ba.order_id",
            "</when>",
            "<when test='bean.businessScope==\"SI\" or bean.businessScope==\"SE\"'>",
            " LEFT JOIN  sc_order ba ON b.order_id=ba.order_id",
            "</when>",
            "<when test='bean.businessScope==\"TI\" or bean.businessScope==\"TE\"'>",
            " LEFT JOIN  tc_order ba ON b.order_id=ba.order_id",
            "</when>",
            "<when test='bean.businessScope==\"LC\"'>",
            " LEFT JOIN  lc_order ba ON b.order_id=ba.order_id",
            "</when>",
            "<when test='bean.businessScope==\"IO\"'>",
            " LEFT JOIN  io_order ba ON b.order_id=ba.order_id",
            "</when>",

            " LEFT JOIN",
            " (SELECT income_writeoff_id,SUM(functional_amount_writeoff) functional_amount_writeoff FROM css_income_writeoff_detail",
            " GROUP BY income_writeoff_id ) d ON a.income_writeoff_id=d.income_writeoff_id",
            " WHERE 1=1 AND a.statement_id IS NULL  and a.org_id=#{bean.orgId} ",
            "<when test='bean.businessScope!=null and bean.businessScope!=\"\"'>",
            " AND a.business_scope = #{bean.businessScope}",
            "</when>",
            "<when test='bean.customerName!=null and bean.customerName!=\"\"'>",
            " AND a.customer_name like  \"%\"#{bean.customerName}\"%\"",
            "</when>",
            "<when test='bean.writeoffNum!=null and bean.writeoffNum!=\"\"'>",
            " AND a.writeoff_num like  \"%\"#{bean.writeoffNum}\"%\"",
            "</when>",
            "<when test='bean.debitNoteNum!=null and bean.debitNoteNum!=\"\"'>",
            " AND b.debit_note_num like  \"%\"#{bean.debitNoteNum}\"%\" ",
            "</when>",
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\" and (bean.businessScope==\"AE\" or bean.businessScope==\"AI\")'>",
            " AND (ba.order_code like  \"%\"#{bean.orderCode}\"%\" or ba.awb_number like  \"%\"#{bean.orderCode}\"%\" or ba.hawb_number like  \"%\"#{bean.orderCode}\"%\" ) ",
            "</when>",
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\" and (bean.businessScope==\"SE\" or bean.businessScope==\"SI\")'>",
            " AND (ba.order_code like  \"%\"#{bean.orderCode}\"%\" or ba.mbl_number like  \"%\"#{bean.orderCode}\"%\" or ba.hbl_number like  \"%\"#{bean.orderCode}\"%\" ) ",
            "</when>",
            
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\" and (bean.businessScope==\"TE\" or bean.businessScope==\"TI\")'>",
            " AND ba.order_code like  \"%\"#{bean.orderCode}\"%\" ",
            "</when>",
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\" and bean.businessScope==\"LC\"'>",
            " AND ba.order_code like  \"%\"#{bean.orderCode}\"%\" ",
            "</when>",
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\" and bean.businessScope==\"IO\"'>",
            " AND (ba.order_code like  \"%\"#{bean.orderCode}\"%\" or ba.customer_number like \"%\"#{bean.orderCode}\"%\" )",
            "</when>",
            
            "<when test='bean.creatorName!=null and bean.creatorName!=\"\"'>",
            " AND a.creator_name like  \"%\"#{bean.creatorName}\"%\" ",
            "</when>",
            "<when test='bean.writeoffDateStart!=null and bean.writeoffDateStart!=\"\"'>",
            " AND a.writeoff_date  <![CDATA[ >= ]]> #{bean.writeoffDateStart}",
            "</when>",
            "<when test='bean.writeoffDateEnd!=null and bean.writeoffDateEnd!=\"\"'>",
            " AND a.writeoff_date <![CDATA[ <= ]]> #{bean.writeoffDateEnd}",
            "</when>",
            "<when test='bean.createTimeBegin!=null and bean.createTimeBegin!=\"\"'>",
            " AND a.create_time  <![CDATA[ >= ]]> #{bean.createTimeBegin}",
            "</when>",
            "<when test='bean.createTimeEnd!=null and bean.createTimeEnd!=\"\"'>",
            " AND a.create_time <![CDATA[ <= ]]> #{bean.createTimeEnd}",
            "</when>",
            " UNION ALL ",
            "    SELECT a.* ",
            " ,c.statement_num debitNoteNumStatementNum",
            " ,c.functional_amount",
            " ,d.functional_amount_writeoff",
            " FROM css_income_writeoff a",
            " LEFT JOIN css_statement c ON a.statement_id=c.statement_id",
            " INNER JOIN ",
            " ( SELECT ca.statement_id FROM css_debit_note ca",
            "<when test='bean.businessScope==\"AI\" or bean.businessScope==\"AE\"'>",
            "  INNER JOIN  af_order cb ON ca.order_id=cb.order_id",
            "</when>",
            "<when test='bean.businessScope==\"SI\" or bean.businessScope==\"SE\"'>",
            "  INNER JOIN  sc_order cb ON ca.order_id=cb.order_id",
            "</when>",

            "<when test='bean.businessScope==\"TI\" or bean.businessScope==\"TE\"'>",
            "  INNER JOIN  tc_order cb ON ca.order_id=cb.order_id",
            "</when>",
            "<when test='bean.businessScope==\"LC\"'>",
            "  INNER JOIN  lc_order cb ON ca.order_id=cb.order_id",
            "</when>",
            "<when test='bean.businessScope==\"IO\"'>",
            "  INNER JOIN  io_order cb ON ca.order_id=cb.order_id",
            "</when>",
            
            "  WHERE 1=1 AND ca.org_id=#{bean.orgId}",
//        "<when test='bean.orderCode!=null and bean.orderCode!=\"\"'>",
//        " AND ( cb.order_code like  \"%\"#{bean.orderCode}\"%\" or cb.awb_number like  \"%\"#{bean.orderCode}\"%\") ",
//        "</when>",
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\" and (bean.businessScope==\"AE\" or bean.businessScope==\"AI\")'>",
            " AND (cb.order_code like  \"%\"#{bean.orderCode}\"%\" or cb.awb_number like  \"%\"#{bean.orderCode}\"%\" or cb.hawb_number like  \"%\"#{bean.orderCode}\"%\" ) ",
            "</when>",
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\" and (bean.businessScope==\"SE\" or bean.businessScope==\"SI\")'>",
            " AND (cb.order_code like  \"%\"#{bean.orderCode}\"%\" or cb.mbl_number like  \"%\"#{bean.orderCode}\"%\" or cb.hbl_number like  \"%\"#{bean.orderCode}\"%\" ) ",
            "</when>",
            
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\" and (bean.businessScope==\"TE\" or bean.businessScope==\"TI\")'>",
            " AND (cb.order_code like  \"%\"#{bean.orderCode}\"%\" or cb.rwb_number like  \"%\"#{bean.orderCode}\"%\") ",
            "</when>",
            
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\" and bean.businessScope==\"TE\"'>",
            " AND (cb.order_code like  \"%\"#{bean.orderCode}\"%\" or cb.customer_number like  \"%\"#{bean.orderCode}\"%\") ",
            "</when>",
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\" and bean.businessScope==\"LC\"'>",
            " AND cb.order_code like  \"%\"#{bean.orderCode}\"%\" ",
            "</when>",
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\" and bean.businessScope==\"IO\"'>",
            " AND (cb.order_code like  \"%\"#{bean.orderCode}\"%\" or cb.customer_number like \"%\"#{bean.orderCode}\"%\"  )",
            "</when>",
            "  GROUP BY ca.statement_id",
            " )ca ON c.statement_id=ca.statement_id",
            " LEFT JOIN",
            " (SELECT income_writeoff_id,SUM(functional_amount_writeoff) functional_amount_writeoff FROM css_income_writeoff_detail",
            " GROUP BY income_writeoff_id ) d ON a.income_writeoff_id=d.income_writeoff_id",
            " WHERE 1=1 AND a.debit_note_id IS NULL and a.org_id=#{bean.orgId} ",
            "<when test='bean.businessScope!=null and bean.businessScope!=\"\"'>",
            " AND a.business_scope = #{bean.businessScope}",
            "</when>",
            "<when test='bean.customerName!=null and bean.customerName!=\"\"'>",
            " AND a.customer_name like  \"%\"#{bean.customerName}\"%\"",
            "</when>",
            "<when test='bean.writeoffNum!=null and bean.writeoffNum!=\"\"'>",
            " AND a.writeoff_num like  \"%\"#{bean.writeoffNum}\"%\"",
            "</when>",
            "<when test='bean.debitNoteNum!=null and bean.debitNoteNum!=\"\"'>",
            " AND c.statement_num like  \"%\"#{bean.debitNoteNum}\"%\" ",
            "</when>",
            "<when test='bean.creatorName!=null and bean.creatorName!=\"\"'>",
            " AND a.creator_name like  \"%\"#{bean.creatorName}\"%\" ",
            "</when>",
            "<when test='bean.writeoffDateStart!=null and bean.writeoffDateStart!=\"\"'>",
            " AND a.writeoff_date  <![CDATA[ >= ]]> #{bean.writeoffDateStart}",
            "</when>",
            "<when test='bean.writeoffDateEnd!=null and bean.writeoffDateEnd!=\"\"'>",
            " AND a.writeoff_date <![CDATA[ <= ]]> #{bean.writeoffDateEnd}",
            "</when>",
            "<when test='bean.createTimeBegin!=null and bean.createTimeBegin!=\"\"'>",
            " AND a.create_time  <![CDATA[ >= ]]> #{bean.createTimeBegin}",
            "</when>",
            "<when test='bean.createTimeEnd!=null and bean.createTimeEnd!=\"\"'>",
            " AND a.create_time <![CDATA[ <= ]]> #{bean.createTimeEnd}",
            "</when>",
            ") a",
            " order by a.income_writeoff_id DESC ",
            "</script>"})
    IPage<CssIncomeWriteoff> getListPage(Page page, @Param("bean") CssIncomeWriteoff bean);

    @Select({"<script>",
//		"select * from (",
            "    SELECT a.* ",
            " ,b.debit_note_num debitNoteNumStatementNum",
            " ,b.functional_amount",
            " ,d.functional_amount_writeoff",
            " FROM css_income_writeoff a",
            " LEFT JOIN  css_debit_note b ON a.debit_note_id=b.debit_note_id",
            "<when test='bean.businessScope==\"AI\" or bean.businessScope==\"AE\"'>",
            " LEFT JOIN  af_order ba ON b.order_id=ba.order_id",
            "</when>",
            "<when test='bean.businessScope==\"SI\" or bean.businessScope==\"SE\"'>",
            " LEFT JOIN  sc_order ba ON b.order_id=ba.order_id",
            "</when>",
            
            "<when test='bean.businessScope==\"TI\" or bean.businessScope==\"TE\"'>",
            " LEFT JOIN  tc_order ba ON b.order_id=ba.order_id",
            "</when>",
            "<when test='bean.businessScope==\"LC\"'>",
            " LEFT JOIN  lc_order ba ON b.order_id=ba.order_id",
            "</when>",

            " LEFT JOIN",
            " (SELECT income_writeoff_id,SUM(functional_amount_writeoff) functional_amount_writeoff FROM css_income_writeoff_detail",
            " GROUP BY income_writeoff_id ) d ON a.income_writeoff_id=d.income_writeoff_id",
            " WHERE 1=1 AND a.statement_id IS NULL  and a.org_id=#{bean.orgId} ",
            "<when test='bean.businessScope!=null and bean.businessScope!=\"\"'>",
            " AND a.business_scope = #{bean.businessScope}",
            "</when>",
            "<when test='bean.customerName!=null and bean.customerName!=\"\"'>",
            " AND a.customer_name like  \"%\"#{bean.customerName}\"%\"",
            "</when>",
            "<when test='bean.writeoffNum!=null and bean.writeoffNum!=\"\"'>",
            " AND a.writeoff_num like  \"%\"#{bean.writeoffNum}\"%\"",
            "</when>",
            "<when test='bean.debitNoteNum!=null and bean.debitNoteNum!=\"\"'>",
            " AND b.debit_note_num like  \"%\"#{bean.debitNoteNum}\"%\" ",
            "</when>",
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\" and (bean.businessScope==\"AE\" or bean.businessScope==\"AI\")'>",
            " AND (ba.order_code like  \"%\"#{bean.orderCode}\"%\" or ba.awb_number like  \"%\"#{bean.orderCode}\"%\" or ba.hawb_number like  \"%\"#{bean.orderCode}\"%\" ) ",
            "</when>",
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\" and (bean.businessScope==\"SE\" or bean.businessScope==\"SI\")'>",
            " AND (ba.order_code like  \"%\"#{bean.orderCode}\"%\" or ba.mbl_number like  \"%\"#{bean.orderCode}\"%\" or ba.hbl_number like  \"%\"#{bean.orderCode}\"%\" ) ",
            "</when>",
            
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\" and (bean.businessScope==\"TE\" or bean.businessScope==\"TI\")'>",
            " AND (ba.order_code like  \"%\"#{bean.orderCode}\"%\" or ba.rwb_number like  \"%\"#{bean.orderCode}\"%\" ) ",
            "</when>",  
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\" and bean.businessScope==\"LC\"'>",
            " AND (ba.order_code like  \"%\"#{bean.orderCode}\"%\" or ba.customer_number like  \"%\"#{bean.orderCode}\"%\" ) ",
            "</when>", 
            
            "<when test='bean.creatorName!=null and bean.creatorName!=\"\"'>",
            " AND a.creator_name like  \"%\"#{bean.creatorName}\"%\" ",
            "</when>",
            "<when test='bean.writeoffDateStart!=null and bean.writeoffDateStart!=\"\"'>",
            " AND a.writeoff_date  <![CDATA[ >= ]]> #{bean.writeoffDateStart}",
            "</when>",
            "<when test='bean.writeoffDateEnd!=null and bean.writeoffDateEnd!=\"\"'>",
            " AND a.writeoff_date <![CDATA[ <= ]]> #{bean.writeoffDateEnd}",
            "</when>",
            "<when test='bean.createTimeBegin!=null and bean.createTimeBegin!=\"\"'>",
            " AND a.create_time  <![CDATA[ >= ]]> #{bean.createTimeBegin}",
            "</when>",
            "<when test='bean.createTimeEnd!=null and bean.createTimeEnd!=\"\"'>",
            " AND a.create_time <![CDATA[ <= ]]> #{bean.createTimeEnd}",
            "</when>",
            " UNION ALL ",
            "    SELECT a.* ",
            " ,c.statement_num debitNoteNumStatementNum",
            " ,c.functional_amount",
            " ,d.functional_amount_writeoff",
            " FROM css_income_writeoff a",
            " LEFT JOIN css_statement c ON a.statement_id=c.statement_id",
            " INNER JOIN ",
            " ( SELECT ca.statement_id FROM css_debit_note ca",
            "<when test='bean.businessScope==\"AI\" or bean.businessScope==\"AE\"'>",
            "  INNER JOIN  af_order cb ON ca.order_id=cb.order_id",
            "</when>",
            "<when test='bean.businessScope==\"SI\" or bean.businessScope==\"SE\"'>",
            "  INNER JOIN  sc_order cb ON ca.order_id=cb.order_id",
            "</when>",
            "<when test='bean.businessScope==\"TI\" or bean.businessScope==\"TE\"'>",
            "  INNER JOIN  tc_order cb ON ca.order_id=cb.order_id",
            "</when>",
            "<when test='bean.businessScope==\"LC\"'>",
            "  INNER JOIN  lc_order cb ON ca.order_id=cb.order_id",
            "</when>",

            "  WHERE 1=1 AND ca.org_id=#{bean.orgId}",
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\" and (bean.businessScope==\"AE\" or bean.businessScope==\"AI\")'>",
            " AND (cb.order_code like  \"%\"#{bean.orderCode}\"%\" or cb.awb_number like  \"%\"#{bean.orderCode}\"%\" or cb.hawb_number like  \"%\"#{bean.orderCode}\"%\" ) ",
            "</when>",
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\" and (bean.businessScope==\"SE\" or bean.businessScope==\"SI\")'>",
            " AND (cb.order_code like  \"%\"#{bean.orderCode}\"%\" or cb.mbl_number like  \"%\"#{bean.orderCode}\"%\" or cb.hbl_number like  \"%\"#{bean.orderCode}\"%\" ) ",
            "</when>",
            
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\" and (bean.businessScope==\"TE\" or bean.businessScope==\"TI\")'>",
            " AND (cb.order_code like  \"%\"#{bean.orderCode}\"%\" or cb.rwb_number like  \"%\"#{bean.orderCode}\"%\" ) ",
            "</when>",
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\" and bean.businessScope==\"LC\"'>",
            " AND (cb.order_code like  \"%\"#{bean.orderCode}\"%\" or cb.customer_number like  \"%\"#{bean.orderCode}\"%\" ) ",
            "</when>",
            
            "  GROUP BY ca.statement_id",
            " )ca ON c.statement_id=ca.statement_id",
            " LEFT JOIN",
            " (SELECT income_writeoff_id,SUM(functional_amount_writeoff) functional_amount_writeoff FROM css_income_writeoff_detail",
            " GROUP BY income_writeoff_id ) d ON a.income_writeoff_id=d.income_writeoff_id",
            " WHERE 1=1 AND a.debit_note_id IS NULL and a.org_id=#{bean.orgId} ",
            "<when test='bean.businessScope!=null and bean.businessScope!=\"\"'>",
            " AND a.business_scope = #{bean.businessScope}",
            "</when>",
            "<when test='bean.customerName!=null and bean.customerName!=\"\"'>",
            " AND a.customer_name like  \"%\"#{bean.customerName}\"%\"",
            "</when>",
            "<when test='bean.writeoffNum!=null and bean.writeoffNum!=\"\"'>",
            " AND a.writeoff_num like  \"%\"#{bean.writeoffNum}\"%\"",
            "</when>",
            "<when test='bean.debitNoteNum!=null and bean.debitNoteNum!=\"\"'>",
            " AND c.statement_num like  \"%\"#{bean.debitNoteNum}\"%\" ",
            "</when>",

            "<when test='bean.creatorName!=null and bean.creatorName!=\"\"'>",
            " AND a.creator_name like  \"%\"#{bean.creatorName}\"%\" ",
            "</when>",
            "<when test='bean.writeoffDateStart!=null and bean.writeoffDateStart!=\"\"'>",
            " AND a.writeoff_date  <![CDATA[ >= ]]> #{bean.writeoffDateStart}",
            "</when>",
            "<when test='bean.writeoffDateEnd!=null and bean.writeoffDateEnd!=\"\"'>",
            " AND a.writeoff_date <![CDATA[ <= ]]> #{bean.writeoffDateEnd}",
            "</when>",
            "<when test='bean.createTimeBegin!=null and bean.createTimeBegin!=\"\"'>",
            " AND a.create_time  <![CDATA[ >= ]]> #{bean.createTimeBegin}",
            "</when>",
            "<when test='bean.createTimeEnd!=null and bean.createTimeEnd!=\"\"'>",
            " AND a.create_time <![CDATA[ <= ]]> #{bean.createTimeEnd}",
            "</when>",
//        ") a",
//        " order by a.income_writeoff_id DESC ",
            "</script>"})
    List<CssIncomeWriteoff> getTatol(@Param("bean") CssIncomeWriteoff bean);

    @Select({"<script>",
            "    SELECT a.currency,SUM(a.amount_writeoff) amount_writeoff ",
            "from (",
            "    SELECT a.* ",
            " ,b.debit_note_num debitNoteNumStatementNum",
            " ,b.functional_amount",
            " ,d.functional_amount_writeoff",
            " FROM css_income_writeoff a",
            " LEFT JOIN  css_debit_note b ON a.debit_note_id=b.debit_note_id",
            "<when test='bean.businessScope==\"AI\" or bean.businessScope==\"AE\"'>",
            " LEFT JOIN  af_order ba ON b.order_id=ba.order_id",
            "</when>",
            "<when test='bean.businessScope==\"SI\" or bean.businessScope==\"SE\"'>",
            " LEFT JOIN  sc_order ba ON b.order_id=ba.order_id",
            "</when>",
            " LEFT JOIN",
            " (SELECT income_writeoff_id,SUM(functional_amount_writeoff) functional_amount_writeoff FROM css_income_writeoff_detail",
            " GROUP BY income_writeoff_id ) d ON a.income_writeoff_id=d.income_writeoff_id",
            " WHERE 1=1 AND a.statement_id IS NULL  and a.org_id=#{bean.orgId} ",
            "<when test='bean.businessScope!=null and bean.businessScope!=\"\"'>",
            " AND a.business_scope = #{bean.businessScope}",
            "</when>",
            "<when test='bean.customerName!=null and bean.customerName!=\"\"'>",
            " AND a.customer_name like  \"%\"#{bean.customerName}\"%\"",
            "</when>",
            "<when test='bean.writeoffNum!=null and bean.writeoffNum!=\"\"'>",
            " AND a.writeoff_num like  \"%\"#{bean.writeoffNum}\"%\"",
            "</when>",
            "<when test='bean.debitNoteNum!=null and bean.debitNoteNum!=\"\"'>",
            " AND b.debit_note_num like  \"%\"#{bean.debitNoteNum}\"%\" ",
            "</when>",
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\" and (bean.businessScope==\"AE\" or bean.businessScope==\"AI\")'>",
            " AND (ba.order_code like  \"%\"#{bean.orderCode}\"%\" or ba.awb_number like  \"%\"#{bean.orderCode}\"%\" or ba.hawb_number like  \"%\"#{bean.orderCode}\"%\" ) ",
            "</when>",
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\" and (bean.businessScope==\"SE\" or bean.businessScope==\"SI\")'>",
            " AND (ba.order_code like  \"%\"#{bean.orderCode}\"%\" or ba.mbl_number like  \"%\"#{bean.orderCode}\"%\" or ba.hbl_number like  \"%\"#{bean.orderCode}\"%\" ) ",
            "</when>",
            "<when test='bean.creatorName!=null and bean.creatorName!=\"\"'>",
            " AND a.creator_name like  \"%\"#{bean.creatorName}\"%\" ",
            "</when>",
            "<when test='bean.writeoffDateStart!=null and bean.writeoffDateStart!=\"\"'>",
            " AND a.writeoff_date  <![CDATA[ >= ]]> #{bean.writeoffDateStart}",
            "</when>",
            "<when test='bean.writeoffDateEnd!=null and bean.writeoffDateEnd!=\"\"'>",
            " AND a.writeoff_date <![CDATA[ <= ]]> #{bean.writeoffDateEnd}",
            "</when>",
            "<when test='bean.createTimeBegin!=null and bean.createTimeBegin!=\"\"'>",
            " AND a.create_time  <![CDATA[ >= ]]> #{bean.createTimeBegin}",
            "</when>",
            "<when test='bean.createTimeEnd!=null and bean.createTimeEnd!=\"\"'>",
            " AND a.create_time <![CDATA[ <= ]]> #{bean.createTimeEnd}",
            "</when>",
            " UNION ALL ",
            "    SELECT a.* ",
            " ,c.statement_num debitNoteNumStatementNum",
            " ,c.functional_amount",
            " ,d.functional_amount_writeoff",
            " FROM css_income_writeoff a",
            " LEFT JOIN css_statement c ON a.statement_id=c.statement_id",
            " INNER JOIN ",
            " ( SELECT ca.statement_id FROM css_debit_note ca",
            "<when test='bean.businessScope==\"AI\" or bean.businessScope==\"AE\"'>",
            "  INNER JOIN  af_order cb ON ca.order_id=cb.order_id",
            "</when>",
            "<when test='bean.businessScope==\"SI\" or bean.businessScope==\"SE\"'>",
            "  INNER JOIN  sc_order cb ON ca.order_id=cb.order_id",
            "</when>",
            "  WHERE 1=1 AND ca.org_id=#{bean.orgId}",
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\" and (bean.businessScope==\"AE\" or bean.businessScope==\"AI\")'>",
            " AND (cb.order_code like  \"%\"#{bean.orderCode}\"%\" or cb.awb_number like  \"%\"#{bean.orderCode}\"%\" or cb.hawb_number like  \"%\"#{bean.orderCode}\"%\" ) ",
            "</when>",
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\" and (bean.businessScope==\"SE\" or bean.businessScope==\"SI\")'>",
            " AND (cb.order_code like  \"%\"#{bean.orderCode}\"%\" or cb.mbl_number like  \"%\"#{bean.orderCode}\"%\" or cb.hbl_number like  \"%\"#{bean.orderCode}\"%\" ) ",
            "</when>",
            "  GROUP BY ca.statement_id",
            " )ca ON c.statement_id=ca.statement_id",
            " LEFT JOIN",
            " (SELECT income_writeoff_id,SUM(functional_amount_writeoff) functional_amount_writeoff FROM css_income_writeoff_detail",
            " GROUP BY income_writeoff_id ) d ON a.income_writeoff_id=d.income_writeoff_id",
            " WHERE 1=1 AND a.debit_note_id IS NULL and a.org_id=#{bean.orgId} ",
            "<when test='bean.businessScope!=null and bean.businessScope!=\"\"'>",
            " AND a.business_scope = #{bean.businessScope}",
            "</when>",
            "<when test='bean.customerName!=null and bean.customerName!=\"\"'>",
            " AND a.customer_name like  \"%\"#{bean.customerName}\"%\"",
            "</when>",
            "<when test='bean.writeoffNum!=null and bean.writeoffNum!=\"\"'>",
            " AND a.writeoff_num like  \"%\"#{bean.writeoffNum}\"%\"",
            "</when>",
            "<when test='bean.debitNoteNum!=null and bean.debitNoteNum!=\"\"'>",
            " AND c.statement_num like  \"%\"#{bean.debitNoteNum}\"%\" ",
            "</when>",

            "<when test='bean.creatorName!=null and bean.creatorName!=\"\"'>",
            " AND a.creator_name like  \"%\"#{bean.creatorName}\"%\" ",
            "</when>",
            "<when test='bean.writeoffDateStart!=null and bean.writeoffDateStart!=\"\"'>",
            " AND a.writeoff_date  <![CDATA[ >= ]]> #{bean.writeoffDateStart}",
            "</when>",
            "<when test='bean.writeoffDateEnd!=null and bean.writeoffDateEnd!=\"\"'>",
            " AND a.writeoff_date <![CDATA[ <= ]]> #{bean.writeoffDateEnd}",
            "</when>",
            "<when test='bean.createTimeBegin!=null and bean.createTimeBegin!=\"\"'>",
            " AND a.create_time  <![CDATA[ >= ]]> #{bean.createTimeBegin}",
            "</when>",
            "<when test='bean.createTimeEnd!=null and bean.createTimeEnd!=\"\"'>",
            " AND a.create_time <![CDATA[ <= ]]> #{bean.createTimeEnd}",
            "</when>",
            ") a",
            " GROUP BY a.currency ",
            "</script>"})
    List<CssIncomeWriteoff> getTatol2(@Param("bean") CssIncomeWriteoff bean);

    @Select({"<script>",
            "select * from css_income_writeoff_detail\n",
            "	where income_writeoff_id = #{income_writeoff_id}",
            "</script>"})
    List<CssIncomeWriteoffDetail> getDetailList(@Param("income_writeoff_id") Integer income_writeoff_id);

    @Select({"<script>",
            "select * from css_income_writeoff_detail\n",
            "	where debit_note_id = #{debit_note_id}",
            "</script>"})
    List<CssIncomeWriteoffDetail> getDetailList2(@Param("debit_note_id") Integer debit_note_id);

    @Select({"<script>",
            "select * from css_income_writeoff_detail\n",
            "	where statement_id = #{statement_id}",
            "</script>"})
    List<CssIncomeWriteoffDetail> getDetailList3(@Param("statement_id") Integer statement_id);

    @Select({"<script>",
            "SELECT a.currency,a.amount_writeoff amountWriteoff2,a.functional_amount_writeoff functionalAmountWriteoff2,",
            "b.exchange_rate,b.amount,b.functional_amount  ",
            " FROM css_income_writeoff_detail a",
            " LEFT JOIN css_debit_note_currency b ON a.debit_note_id=b.debit_note_id AND a.currency =b.currency",
            "where b.org_id=#{org_id} and a.income_writeoff_id=#{income_writeoff_id}",
            "</script>"})
    List<CssDebitNoteCurrency> queryBillDetail(@Param("org_id") Integer org_id, @Param("income_writeoff_id") Integer income_writeoff_id);

    @Select({"<script>",
            "SELECT a.currency,a.amount_writeoff amountWriteoff2,a.functional_amount_writeoff functionalAmountWriteoff2,",
            "b.exchange_rate,b.amount,b.functional_amount  ",
            " FROM css_income_writeoff_detail a",
            " LEFT JOIN css_statement_currency b ON a.statement_id=b.statement_id AND a.currency =b.currency",
            "where b.org_id=#{org_id} and a.income_writeoff_id=#{income_writeoff_id}",
            "</script>"})
    List<StatementCurrency> queryListDetail(@Param("org_id") Integer org_id, @Param("income_writeoff_id") Integer income_writeoff_id);

    @Select({"<script>",
            "SELECT a.debit_note_id,IFNULL(a.functional_amount_writeoff,0) functional_amount_writeoff ,a.functional_amount - IFNULL(a.functional_amount_writeoff,0) functionalAmountWriteoffNo ",
            " FROM css_debit_note a",
            "where a.org_id=#{org_id} and a.statement_id=#{statement_id}",
            "</script>"})
    List<CssDebitNote> getUpdateList2(@Param("org_id") Integer org_id, @Param("statement_id") Integer statement_id);

    @Select({"<script>",
            "SELECT a.debit_note_id,IFNULL(a.functional_amount_writeoff,0) functional_amount_writeoff ,a.functional_amount - IFNULL(a.functional_amount_writeoff,0) functionalAmountWriteoffNo ",
            " FROM css_debit_note a",
            "<when test='businessScope==\"AI\" or businessScope==\"AE\"'>",
            " left join af_order b on a.order_id=b.order_id ",
            "</when>",
            "<when test='businessScope==\"SI\" or businessScope==\"SE\"'>",
            " left join sc_order b on a.order_id=b.order_id ",
            "</when>",
            "<when test='businessScope==\"TI\" or businessScope==\"TE\"'>",
            " left join tc_order b on a.order_id=b.order_id ",
            "</when>",
            "where a.org_id=#{org_id} and a.statement_id=#{statement_id}",
            "<when test='businessScope==\"AI\" or businessScope==\"SI\"'>",
            " order by b.expect_arrival ,a.debit_note_id",
            "</when>",
            "<when test='businessScope==\"AE\" or businessScope==\"SE\"'>",
            " order by b.expect_departure ,a.debit_note_id",
            "</when>",

            "</script>"})
    List<CssDebitNote> getUpdateList3(@Param("org_id") Integer org_id, @Param("statement_id") Integer statement_id, @Param("businessScope") String businessScope);

    @Select({"<script>",
            "SELECT a.debit_note_id,IFNULL(a.functional_amount_writeoff,0) functional_amount_writeoff ,a.functional_amount - IFNULL(a.functional_amount_writeoff,0) functionalAmountWriteoffNo ",
            " FROM css_debit_note a",
            "<when test='businessScope==\"AI\" or businessScope==\"AE\"'>",
            " left join af_order b on a.order_id=b.order_id ",
            "</when>",
            "<when test='businessScope==\"SI\" or businessScope==\"SE\"'>",
            " left join sc_order b on a.order_id=b.order_id ",
            "</when>",
            "<when test='businessScope==\"TI\" or businessScope==\"TE\"'>",
            " left join tc_order b on a.order_id=b.order_id ",
            "</when>",
            "where a.org_id=#{org_id} and a.statement_id=#{statement_id}",
            "<when test='businessScope==\"AI\" or businessScope==\"SI\"'>",
            " order by b.expect_arrival ,a.debit_note_id desc",
            "</when>",
            "<when test='businessScope==\"AE\" or businessScope==\"SE\" or businessScope==\"TE\"'>",
            " order by b.expect_departure ,a.debit_note_id desc",
            "</when>",

            "</script>"})
    List<CssDebitNote> getUpdateList3ForDelete(@Param("org_id") Integer org_id, @Param("statement_id") Integer statement_id, @Param("businessScope") String businessScope);


    @Update({"<script>",
            " update css_debit_note",
            " set functional_amount_writeoff=functional_amount ,writeoff_complete=1",
            "where org_id=#{org_id} and statement_id=#{statement_id}",
            "</script>"})
    void updateNote(@Param("org_id") Integer org_id, @Param("statement_id") Integer statement_id);

    @Update({"<script>",
            " update css_debit_note",
            " set functional_amount_writeoff=functional_amount ",
            "where org_id=#{org_id} and debit_note_id=#{debit_note_id}",
            "</script>"})
    void updateNote2(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id);

    @Update({"<script>",
            " update css_debit_note",
            " set writeoff_complete=1",
            "where org_id=#{org_id} and debit_note_id=#{debit_note_id}",
            "</script>"})
    void updateNote33(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id);

    @Update({"<script>",
            " update css_debit_note",
            " set functional_amount_writeoff=#{functional_amount_writeoff} ,writeoff_complete=0",
            "where org_id=#{org_id} and debit_note_id=#{debit_note_id}",
            "</script>"})
    void updateNote3(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id, @Param("functional_amount_writeoff") BigDecimal functional_amount_writeoff);

    @Update({"<script>",
            " update css_debit_note",
            " set functional_amount_writeoff=#{functional_amount_writeoff} ,writeoff_complete=NULL",
            "where org_id=#{org_id} and debit_note_id=#{debit_note_id}",
            "</script>"})
    void updateNote4(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id, @Param("functional_amount_writeoff") BigDecimal functional_amount_writeoff);

    @Update({"<script>",
            " update css_debit_note_currency",
            " set amount_writeoff=amount,functional_amount_writeoff=functional_amount ",
            "where org_id=#{org_id} and debit_note_id=#{debit_note_id}",
            "</script>"})
    void updateNoteCurrency(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id);

    @Update({"<script>",
            " update css_debit_note_currency",
            " set amount_writeoff=amount,functional_amount_writeoff=functional_amount ",
            "where org_id=#{org_id} and debit_note_id=#{debit_note_id} and currency=#{currency}",
            "</script>"})
    void updateNoteCurrency2(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id, @Param("currency") String currency);

    @Update({"<script>",
            " update css_debit_note_currency",
            " set amount_writeoff=amount,functional_amount_writeoff=functional_amount ",
            "where org_id=#{org_id} and functional_amount &lt; 0 and debit_note_id=#{debit_note_id} and currency=#{currency}",
            "</script>"})
    void updateNoteCurrency22(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id, @Param("currency") String currency);

    @Update({"<script>",
            " update css_debit_note_currency",
            " set amount_writeoff=NULL,functional_amount_writeoff=NULL ",
            "where org_id=#{org_id} and functional_amount &lt; 0 and debit_note_id=#{debit_note_id} and currency=#{currency}",
            "</script>"})
    void updateNoteCurrency33(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id, @Param("currency") String currency);

    @Update({"<script>",
            " update css_debit_note_currency",
            " set amount_writeoff=#{amount_writeoff},functional_amount_writeoff=#{functional_amount_writeoff} ",
            "where org_id=#{org_id} and debit_note_id=#{debit_note_id} and currency=#{currency}",
            "</script>"})
    void updateNoteCurrency5(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id, @Param("currency") String currency
            , @Param("amount_writeoff") BigDecimal amount_writeoff, @Param("functional_amount_writeoff") BigDecimal functional_amount_writeoff);

    @Select({"<script>",
            " select functional_amount,amount,IFNULL(functional_amount_writeoff,0) functional_amount_writeoff ",
            ",IFNULL(amount_writeoff,0) amountWriteoff",
            ",amount - IFNULL(amount_writeoff,0) amountWriteoffNo",
            ",functional_amount - IFNULL(functional_amount_writeoff,0) functionalAmountWriteoffNo",
            "from  css_debit_note_currency",
            "where org_id=#{org_id} and debit_note_id=#{debit_note_id} ",
            "</script>"})
    List<CssDebitNoteCurrency> getCurrencyList(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id);

    @Select({"<script>",
            " select functional_amount,amount,IFNULL(functional_amount_writeoff,0) functional_amount_writeoff ",
            ",IFNULL(amount_writeoff,0) amountWriteoff",
            ",amount - IFNULL(amount_writeoff,0) amountWriteoffNo",
            ",functional_amount - IFNULL(functional_amount_writeoff,0) functionalAmountWriteoffNo",
            "from  css_debit_note_currency",
            "where org_id=#{org_id} and functional_amount &lt; 0 and amount_writeoff is null and debit_note_id=#{debit_note_id} and currency=#{currency}",
            "</script>"})
    List<CssDebitNoteCurrency> getCurrencyList2(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id, @Param("currency") String currency);

    @Select({"<script>",
            " select functional_amount,amount,IFNULL(functional_amount_writeoff,0) functional_amount_writeoff ",
            ",IFNULL(amount_writeoff,0) amountWriteoff",
            ",amount - IFNULL(amount_writeoff,0) amountWriteoffNo",
            ",functional_amount - IFNULL(functional_amount_writeoff,0) functionalAmountWriteoffNo",
            "from  css_debit_note_currency",
            "where org_id=#{org_id} and functional_amount &lt; 0 and amount_writeoff is not null and debit_note_id=#{debit_note_id} and currency=#{currency}",
            "</script>"})
    List<CssDebitNoteCurrency> getCurrencyList2ForDeleteList(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id, @Param("currency") String currency);

    @Select({"<script>",
            " select functional_amount,amount,IFNULL(functional_amount_writeoff,0) functional_amount_writeoff ",
            ",IFNULL(amount_writeoff,0) amountWriteoff",
            ",amount - IFNULL(amount_writeoff,0) amountWriteoffNo",
            ",functional_amount - IFNULL(functional_amount_writeoff,0) functionalAmountWriteoffNo",
            "from  css_debit_note_currency",
            "where org_id=#{org_id} and (functional_amount &gt; 0 or functional_amount=0) and debit_note_id=#{debit_note_id} and currency=#{currency}",
            "</script>"})
    List<CssDebitNoteCurrency> getCurrencyList22(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id, @Param("currency") String currency);

    @Select({"<script>",
            " select functional_amount,amount,IFNULL(functional_amount_writeoff,0) functional_amount_writeoff ",
            ",IFNULL(amount_writeoff,0) amountWriteoff",
            ",amount - IFNULL(amount_writeoff,0) amountWriteoffNo",
            ",functional_amount - IFNULL(functional_amount_writeoff,0) functionalAmountWriteoffNo",
            "from  css_debit_note_currency",
            "where org_id=#{org_id} and functional_amount &gt; 0 and debit_note_id=#{debit_note_id} and currency=#{currency}",
            "</script>"})
    List<CssDebitNoteCurrency> getCurrencyList22ForDelete(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id, @Param("currency") String currency);

    @Update({"<script>",
            " update css_debit_note",
            " set functional_amount_writeoff=NULL ,writeoff_complete=NULL",
            "where org_id=#{org_id} and statement_id=#{statement_id}",
            "</script>"})
    void updateNote22(@Param("org_id") Integer org_id, @Param("statement_id") Integer statement_id);

    @Update({"<script>",
            " update css_debit_note",
            " set functional_amount_writeoff=NULL ",
            "where org_id=#{org_id} and debit_note_id=#{debit_note_id}",
            "</script>"})
    void updateNote23(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id);

    @Update({"<script>",
            " update css_debit_note_currency",
            " set amount_writeoff=NULL,functional_amount_writeoff=NULL ",
            "where org_id=#{org_id} and debit_note_id=#{debit_note_id}",
            "</script>"})
    void updateNoteCurrency4(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id);

    @Update({"<script>",
            " update css_debit_note_currency",
            " set amount_writeoff=NULL,functional_amount_writeoff=NULL ",
            "where org_id=#{org_id} and debit_note_id=#{debit_note_id} and currency=#{currency}",
            "</script>"})
    void updateNoteCurrency3(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id, @Param("currency") String currency);
}
