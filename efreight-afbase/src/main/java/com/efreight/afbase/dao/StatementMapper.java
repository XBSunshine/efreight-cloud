package com.efreight.afbase.dao;

import com.efreight.afbase.entity.CssDebitNote;
import com.efreight.afbase.entity.DebitNote;
import com.efreight.afbase.entity.Statement;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.StatementPrint;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.StatementType;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * CSS 应收：清单 Mapper 接口
 * </p>
 *
 * @author xiaobo
 * @since 2019-12-24
 */
public interface StatementMapper extends BaseMapper<Statement> {

    @Select("select tom_F_format_money(#{amount})")
    String calltomFFormatMoney(@Param("amount") BigDecimal amount);

    @Select({"<script>",
            "    SELECT a.* ",
//        ",b.Awb_Number,b.Order_Code,b.Customer_Number ",
            "FROM css_statement a",
            "INNER JOIN ",
            " ( select c.statement_id  from css_debit_note c",

            "<when test='bean.businessScope==\"AI\" or bean.businessScope==\"AE\"'>",
            "   INNER JOIN af_order b ON c.order_id=b.order_id",
            "</when>",
            "<when test='bean.businessScope==\"SI\" or bean.businessScope==\"SE\"'>",
            "   INNER JOIN sc_order b ON c.order_id=b.order_id",
            "</when>",
            "<when test='bean.businessScope==\"TI\" or bean.businessScope==\"TE\"'>",
            "   INNER JOIN tc_order b ON c.order_id=b.order_id",
            "</when>",
            "<when test='bean.businessScope==\"LC\"'>",
            "   INNER JOIN lc_order b ON c.order_id=b.order_id",
            "</when>",
            "<when test='bean.businessScope==\"IO\"'>",
            "   INNER JOIN io_order b ON c.order_id=b.order_id",
            "</when>",

            " where 1=1 and c.org_id=#{bean.orgId}",
            "<when test='bean.debitNoteNum!=null and bean.debitNoteNum!=\"\"'>",
            " AND c.debit_note_num like  \"%\"#{bean.debitNoteNum}\"%\"",
            "</when>",
            "<when test='bean.awbNumber!=null and bean.awbNumber!=\"\" and bean.businessScope==\"AE\"'>",
            " AND b.awb_number like  \"%\"#{bean.awbNumber}\"%\"",
            "</when>",
            "<when test='bean.awbNumber!=null and bean.awbNumber!=\"\" and bean.businessScope==\"SE\"'>",
            " AND b.mbl_number like  \"%\"#{bean.awbNumber}\"%\"",
            "</when>",
            "<when test='bean.awbNumber!=null and bean.awbNumber!=\"\" and bean.businessScope==\"TE\"'>",
            " AND b.rwb_number like  \"%\"#{bean.awbNumber}\"%\"",
            "</when>",
            "<when test='bean.awbNumber!=null and bean.awbNumber!=\"\" and bean.businessScope==\"AI\"'>",
            " AND (b.awb_number like  \"%\"#{bean.awbNumber}\"%\" or b.hawb_number like  \"%\"#{bean.awbNumber}\"%\")",
            "</when>",
            "<when test='bean.awbNumber!=null and bean.awbNumber!=\"\" and bean.businessScope==\"SI\" '>",
            " AND (b.mbl_number like  \"%\"#{bean.awbNumber}\"%\" or b.hbl_number like  \"%\"#{bean.awbNumber}\"%\")",
            "</when>",
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\"'>",
            " AND b.order_code like  \"%\"#{bean.orderCode}\"%\"",
            "</when>",
            "<when test='bean.customerNumber!=null and bean.customerNumber!=\"\"'>",
            " AND b.customer_number like  \"%\"#{bean.customerNumber}\"%\"",
            "</when>",
            "   GROUP BY c.statement_id ",
            ") c ON a.statement_id=c.statement_id",
            " WHERE 1=1 and a.org_id=#{bean.orgId} ",
            "<when test='bean.statementStatus==\"-2\"'>",
            " AND (a.writeoff_complete =0 or a.writeoff_complete IS NULL)",
            "</when>",
            "<when test='bean.statementStatus==\"-1\"'>",
            " AND a.writeoff_complete IS NULL",
            "</when>",
            "<when test='bean.statementStatus==\"1\"'>",
            " AND a.writeoff_complete=1",
            "</when>",
            "<when test='bean.statementStatus==\"0\"'>",
            " AND a.writeoff_complete=0",
            "</when>",
            "<when test='bean.businessScope!=null and bean.businessScope!=\"\"'>",
            " AND a.business_scope = #{bean.businessScope}",
            "</when>",
            "<when test='bean.customerName!=null and bean.customerName!=\"\"'>",
            " AND a.customer_name like  \"%\"#{bean.customerName}\"%\"",
            "</when>",
            "<when test='bean.statementNum!=null and bean.statementNum!=\"\"'>",
            " AND a.statement_num like  \"%\"#{bean.statementNum}\"%\"",
            "</when>",
            "<when test='bean.creatorName!=null and bean.creatorName!=\"\"'>",
            " AND a.creator_name like  \"%\"#{bean.creatorName}\"%\"",
            "</when>",
            "<when test='bean.statementDateStart!=null and bean.statementDateStart!=\"\"'>",
            " AND a.statement_date  <![CDATA[ >= ]]> #{bean.statementDateStart}",
            "</when>",
            "<when test='bean.statementDateEnd!=null and bean.statementDateEnd!=\"\"'>",
            " AND a.statement_date <![CDATA[ <= ]]> #{bean.statementDateEnd}",
            "</when>",
            "<when test='bean.invoiceNum!=null and bean.invoiceNum!=\"\"'>",
            " AND a.invoice_num like  \"%\"#{bean.invoiceNum}\"%\"",
            "</when>",
            "<when test='bean.invoiceTitle!=null and bean.invoiceTitle!=\"\"'>",
            " AND a.invoice_title like  \"%\"#{bean.invoiceTitle}\"%\"",
            "</when>",
            "<when test='bean.invoiceDateStart!=null and bean.invoiceDateStart!=\"\"'>",
            " AND a.invoice_date  <![CDATA[ >= ]]> #{bean.invoiceDateStart}",
            "</when>",
            "<when test='bean.invoiceDateEnd!=null and bean.invoiceDateEnd!=\"\"'>",
            " AND a.invoice_date <![CDATA[ <= ]]> #{bean.invoiceDateEnd}",
            "</when>",
            "order by a.statement_id DESC",
            "</script>"})
    IPage<Statement> getPage(Page page, @Param("bean") Statement bean);

    @Select({"<script>",
            "    SELECT a.* ",
//        ",b.Awb_Number,b.Order_Code,b.Customer_Number ",
            "FROM css_statement a",
            "INNER JOIN ",
            " ( select c.statement_id  from css_debit_note c",

            "<when test='bean.businessScope==\"AI\" or bean.businessScope==\"AE\"'>",
            "   INNER JOIN af_order b ON c.order_id=b.order_id",
            "</when>",
            "<when test='bean.businessScope==\"SI\" or bean.businessScope==\"SE\"'>",
            "   INNER JOIN sc_order b ON c.order_id=b.order_id",
            "</when>",
            "<when test='bean.businessScope==\"TI\" or bean.businessScope==\"TE\"'>",
            "   INNER JOIN tc_order b ON c.order_id=b.order_id",
            "</when>",
            "<when test='bean.businessScope==\"LC\"'>",
            "   INNER JOIN lc_order b ON c.order_id=b.order_id",
            "</when>",
            "<when test='bean.businessScope==\"IO\"'>",
            "   INNER JOIN io_order b ON c.order_id=b.order_id",
            "</when>",

            " where 1=1 and c.org_id=#{bean.orgId}",
            "<when test='bean.debitNoteNum!=null and bean.debitNoteNum!=\"\"'>",
            " AND c.debit_note_num like  \"%\"#{bean.debitNoteNum}\"%\"",
            "</when>",
            "<when test='bean.awbNumber!=null and bean.awbNumber!=\"\" and bean.businessScope==\"AE\"'>",
            " AND b.awb_number like  \"%\"#{bean.awbNumber}\"%\"",
            "</when>",

            "<when test='bean.awbNumber!=null and bean.awbNumber!=\"\" and bean.businessScope==\"SE\"'>",
            " AND b.mbl_number like  \"%\"#{bean.awbNumber}\"%\"",
            "</when>",
            "<when test='bean.awbNumber!=null and bean.awbNumber!=\"\" and bean.businessScope==\"TE\"'>",
            " AND b.rwb_number like  \"%\"#{bean.awbNumber}\"%\"",
            "</when>",
            "<when test='bean.awbNumber!=null and bean.awbNumber!=\"\" and bean.businessScope==\"LC\"'>",
            " AND b.customer_number like  \"%\"#{bean.awbNumber}\"%\"",
            "</when>",
            "<when test='bean.awbNumber!=null and bean.awbNumber!=\"\" and bean.businessScope==\"AI\"'>",
            " AND (b.awb_number like  \"%\"#{bean.awbNumber}\"%\" or b.hawb_number like  \"%\"#{bean.awbNumber}\"%\")",
            "</when>",
            "<when test='bean.awbNumber!=null and bean.awbNumber!=\"\" and bean.businessScope==\"SI\" '>",
            " AND (b.mbl_number like  \"%\"#{bean.awbNumber}\"%\" or b.hbl_number like  \"%\"#{bean.awbNumber}\"%\")",
            "</when>",
            "<when test='bean.orderCode!=null and bean.orderCode!=\"\"'>",
            " AND b.order_code like  \"%\"#{bean.orderCode}\"%\"",
            "</when>",
            "<when test='bean.customerNumber!=null and bean.customerNumber!=\"\"'>",
            " AND b.customer_number like  \"%\"#{bean.customerNumber}\"%\"",
            "</when>",
            "   GROUP BY c.statement_id ",
            ") c ON a.statement_id=c.statement_id",
            " WHERE 1=1 and a.org_id=#{bean.orgId} ",
            "<when test='bean.statementStatus==\"-2\"'>",
            " AND (a.writeoff_complete =0 or a.writeoff_complete IS NULL)",
            "</when>",
            "<when test='bean.statementStatus==\"-1\"'>",
            " AND a.writeoff_complete IS NULL",
            "</when>",
            "<when test='bean.statementStatus==\"1\"'>",
            " AND a.writeoff_complete=1",
            "</when>",
            "<when test='bean.statementStatus==\"0\"'>",
            " AND a.writeoff_complete=0",
            "</when>",
            "<when test='bean.businessScope!=null and bean.businessScope!=\"\"'>",
            " AND a.business_scope = #{bean.businessScope}",
            "</when>",
            "<when test='bean.customerName!=null and bean.customerName!=\"\"'>",
            " AND a.customer_name like  \"%\"#{bean.customerName}\"%\"",
            "</when>",
            "<when test='bean.statementNum!=null and bean.statementNum!=\"\"'>",
            " AND a.statement_num like  \"%\"#{bean.statementNum}\"%\"",
            "</when>",
            "<when test='bean.creatorName!=null and bean.creatorName!=\"\"'>",
            " AND a.creator_name like  \"%\"#{bean.creatorName}\"%\"",
            "</when>",
            "<when test='bean.statementDateStart!=null and bean.statementDateStart!=\"\"'>",
            " AND a.statement_date  <![CDATA[ >= ]]> #{bean.statementDateStart}",
            "</when>",
            "<when test='bean.statementDateEnd!=null and bean.statementDateEnd!=\"\"'>",
            " AND a.statement_date <![CDATA[ <= ]]> #{bean.statementDateEnd}",
            "</when>",
            "<when test='bean.invoiceNum!=null and bean.invoiceNum!=\"\"'>",
            " AND a.invoice_num like  \"%\"#{bean.invoiceNum}\"%\"",
            "</when>",
            "<when test='bean.invoiceTitle!=null and bean.invoiceTitle!=\"\"'>",
            " AND a.invoice_title like  \"%\"#{bean.invoiceTitle}\"%\"",
            "</when>",
            "<when test='bean.invoiceDateStart!=null and bean.invoiceDateStart!=\"\"'>",
            " AND a.invoice_date  <![CDATA[ >= ]]> #{bean.invoiceDateStart}",
            "</when>",
            "<when test='bean.invoiceDateEnd!=null and bean.invoiceDateEnd!=\"\"'>",
            " AND a.invoice_date <![CDATA[ <= ]]> #{bean.invoiceDateEnd}",
            "</when>",
            "order by a.statement_id DESC",
            "</script>"})
    List<Statement> getPageList(@Param("bean") Statement bean);

    @Select({"<script>",
            "select * from css_statement\n",
            "	where org_id = #{org_id} and statement_num like  \"%\"#{statement_num}\"%\"",
            " ORDER BY statement_id DESC ",
            "</script>"})
    List<Statement> selectCode(@Param("org_id") Integer org_id, @Param("statement_num") String statement_num);

    @Select({"<script>",
            "select * from css_debit_note\n",
            " where   org_id=#{org_id} and debit_note_id in (${ids}) and statement_id IS NOT NULL",
            "</script>"})
    List<CssDebitNote> isDebitNote(@Param("org_id") Integer org_id, @Param("ids") String ids);

    @Update("update css_debit_note set\n"
            + " statement_id=#{statement_id},row_uuid=#{row_uuid} "
            + " where   org_id=#{org_id} and debit_note_id in (${ids}) ")
    void updateDebitNote(@Param("org_id") Integer org_id, @Param("ids") String ids, @Param("statement_id") Integer statement_id, @Param("row_uuid") String row_uuid);

    @Update("update css_debit_note set\n"
            + " statement_id=NULL "
            + " where   org_id=#{org_id} and statement_id = #{statement_id} ")
    void updateDebitNote2(@Param("org_id") Integer org_id, @Param("statement_id") Integer statement_id);

    @Delete("delete from css_statement_currency \n"
            + " where   org_id=#{org_id} and statement_id = #{statement_id} ")
    void delete2(@Param("org_id") Integer org_id, @Param("statement_id") Integer statement_id);

    @Select({"<script>",
            "SELECT ",
            "<when test='businessScope==\"AI\" '>",
            "CASE WHEN C.awb_number IS NOT NULL AND C.awb_number !='' AND C.hawb_number IS NOT NULL AND C.hawb_number !='' THEN CONCAT(C.awb_number,'_', C.hawb_number)",
            "WHEN C.awb_number IS NOT NULL AND C.awb_number !='' THEN C.awb_number ",
            "WHEN C.hawb_number IS NOT NULL AND C.hawb_number !='' THEN C.hawb_number",
            "ELSE '' END awbNumber",
            ",C.expect_arrival FlightDate",
            "</when>",
            "<when test='businessScope==\"AE\" '>",
            "C.awb_number,C.expect_arrival flightDate",
            "</when>",
            "<when test='businessScope==\"SI\"'>",
            "CASE WHEN C.mbl_number IS NOT NULL AND C.mbl_number!='' AND C.hbl_number IS NOT NULL AND C.hbl_number !='' THEN CONCAT(C.mbl_number,'_', C.hbl_number)",
            "WHEN C.mbl_number IS NOT NULL AND C.mbl_number !='' THEN C.mbl_number ",
            "WHEN C.hbl_number IS NOT NULL AND C.hbl_number !='' THEN C.hbl_number",
            "ELSE '' END awb_number,",
            ",C.expect_arrival flightDate",
            "</when>",
            "<when test='businessScope==\"SE\"'>",
            "C.mbl_number awb_number,C.Expect_Departure flightDate",
            "</when>",
            ",A.debit_note_num,CONCAT('(',B.currency,')',FORMAT(B.amount,2)) AS currencyAmount,FORMAT(B.functional_amount,2) as functional_amount\n",
            "FROM css_debit_note A\n" +
                    "INNER JOIN css_debit_note_currency B ON A.debit_note_id=B.debit_note_id\n",
            "<when test='businessScope==\"AI\" or businessScope==\"AE\"'>",
            "INNER JOIN af_order C ON A.order_id=C.order_id ",
            "</when>",
            "<when test='businessScope==\"SI\" or businessScope==\"SE\"'>",
            "INNER JOIN sc_order C ON A.order_id=C.order_id ",
            "</when>",
            "WHERE A.org_id=#{orgId} AND A.statement_id=#{statementId}",
            "</script>"})
    List<StatementPrint> queryDebitNoteListByStatementId(@Param("statementId") Integer statementId, @Param("orgId") Integer orgId, @Param("businessScope") String businessScope);

    @Select("SELECT C.org_name,C.org_ename,D.coop_name customer_name,D.coop_ename customer_ename \n" +
            " ,A.statement_num\n" +
            " ,A.statement_date\n" +
            " ,FORMAT(A.functional_amount,2) AS functional_amount\n" +
            " ,B.currency_amount\n" +
            "FROM css_statement A\n" +
            "INNER JOIN (\n" +
            " SELECT statement_id\n" +
            "  ,GROUP_CONCAT(CONCAT('(',currency,')',FORMAT(amount,2))) AS currency_amount\n" +
            " FROM css_statement_currency\n" +
            " GROUP BY statement_id\n" +
            ") B ON A.statement_id=B.statement_id\n" +
            "INNER JOIN hrs_org C ON C.org_id=A.org_id\n" +
            "LEFT JOIN prm_coop D ON D.coop_id=A.customer_id\n" +
            "WHERE A.org_id=#{orgId} AND A.statement_id=#{statementId}")
    StatementPrint queryStatementPrintHeaderInfoByStatementId(@Param("statementId") Integer statementId, @Param("orgId") Integer orgId);

    @Update("update af_income set\n"
            + " income_functional_amount=income_amount*#{income_exchange_rate}"
            + " ,main_routing=if(main_routing is not null,income_amount*#{income_exchange_rate},null)"
            + " ,feeder=if(feeder is not null,income_amount*#{income_exchange_rate},null)"
            + " ,operation=if(operation is not null,income_amount*#{income_exchange_rate},null)"
            + " ,packaging=if(packaging is not null,income_amount*#{income_exchange_rate},null)"
            + " ,storage=if(storage is not null,income_amount*#{income_exchange_rate},null)"
            + " ,postage=if(postage is not null,income_amount*#{income_exchange_rate},null)"
            + " ,clearance=if(clearance is not null,income_amount*#{income_exchange_rate},null)"
            + " ,exchange=if(exchange is not null,income_amount*#{income_exchange_rate},null)"
            + ",income_exchange_rate=#{income_exchange_rate}"
            + " where   org_id=#{org_id} and  debit_note_id in (${debit_note_id}) and income_currency=#{income_currency}")
    void updateIncome2(@Param("org_id") Integer org_id, @Param("debit_note_id") String debit_note_id, @Param("income_currency") String income_currency, @Param("income_exchange_rate") BigDecimal income_exchange_rate);

    @Update("update sc_income set\n"
            + " income_functional_amount=income_amount*#{income_exchange_rate}"
            + " ,main_routing=if(main_routing is not null,income_amount*#{income_exchange_rate},null)"
            + " ,feeder=if(feeder is not null,income_amount*#{income_exchange_rate},null)"
            + " ,operation=if(operation is not null,income_amount*#{income_exchange_rate},null)"
            + " ,packaging=if(packaging is not null,income_amount*#{income_exchange_rate},null)"
            + " ,storage=if(storage is not null,income_amount*#{income_exchange_rate},null)"
            + " ,postage=if(postage is not null,income_amount*#{income_exchange_rate},null)"
            + " ,clearance=if(clearance is not null,income_amount*#{income_exchange_rate},null)"
            + " ,exchange=if(exchange is not null,income_amount*#{income_exchange_rate},null)"
            + ",income_exchange_rate=#{income_exchange_rate}"
            + " where   org_id=#{org_id} and  debit_note_id in (${debit_note_id}) and income_currency=#{income_currency}")
    void updateIncomeSE2(@Param("org_id") Integer org_id, @Param("debit_note_id") String debit_note_id, @Param("income_currency") String income_currency, @Param("income_exchange_rate") BigDecimal income_exchange_rate);

    @Update("update tc_income set\n"
            + " income_functional_amount=income_amount*#{income_exchange_rate}"
            + " ,main_routing=if(main_routing is not null,income_amount*#{income_exchange_rate},null)"
            + " ,feeder=if(feeder is not null,income_amount*#{income_exchange_rate},null)"
            + " ,operation=if(operation is not null,income_amount*#{income_exchange_rate},null)"
            + " ,packaging=if(packaging is not null,income_amount*#{income_exchange_rate},null)"
            + " ,storage=if(storage is not null,income_amount*#{income_exchange_rate},null)"
            + " ,postage=if(postage is not null,income_amount*#{income_exchange_rate},null)"
            + " ,clearance=if(clearance is not null,income_amount*#{income_exchange_rate},null)"
            + " ,exchange=if(exchange is not null,income_amount*#{income_exchange_rate},null)"
            + ",income_exchange_rate=#{income_exchange_rate}"
            + " where   org_id=#{org_id} and  debit_note_id in (${debit_note_id}) and income_currency=#{income_currency}")
    void updateIncomeTE2(@Param("org_id") Integer org_id, @Param("debit_note_id") String debit_note_id, @Param("income_currency") String income_currency, @Param("income_exchange_rate") BigDecimal income_exchange_rate);

    @Update("update lc_income set\n"
            + " income_functional_amount=income_amount*#{income_exchange_rate}"
            + " ,main_routing=if(main_routing is not null,income_amount*#{income_exchange_rate},null)"
            + " ,feeder=if(feeder is not null,income_amount*#{income_exchange_rate},null)"
            + " ,operation=if(operation is not null,income_amount*#{income_exchange_rate},null)"
            + " ,packaging=if(packaging is not null,income_amount*#{income_exchange_rate},null)"
            + " ,storage=if(storage is not null,income_amount*#{income_exchange_rate},null)"
            + " ,postage=if(postage is not null,income_amount*#{income_exchange_rate},null)"
            + " ,clearance=if(clearance is not null,income_amount*#{income_exchange_rate},null)"
            + " ,exchange=if(exchange is not null,income_amount*#{income_exchange_rate},null)"
            + ",income_exchange_rate=#{income_exchange_rate}"
            + " where   org_id=#{org_id} and  debit_note_id in (${debit_note_id}) and income_currency=#{income_currency}")
    void updateIncomeLC2(@Param("org_id") Integer org_id, @Param("debit_note_id") String debit_note_id, @Param("income_currency") String income_currency, @Param("income_exchange_rate") BigDecimal income_exchange_rate);

    @Update("update io_income set\n"
            + " income_functional_amount=income_amount*#{income_exchange_rate}"
            + " ,main_routing=if(main_routing is not null,income_amount*#{income_exchange_rate},null)"
            + " ,feeder=if(feeder is not null,income_amount*#{income_exchange_rate},null)"
            + " ,operation=if(operation is not null,income_amount*#{income_exchange_rate},null)"
            + " ,packaging=if(packaging is not null,income_amount*#{income_exchange_rate},null)"
            + " ,storage=if(storage is not null,income_amount*#{income_exchange_rate},null)"
            + " ,postage=if(postage is not null,income_amount*#{income_exchange_rate},null)"
            + " ,clearance=if(clearance is not null,income_amount*#{income_exchange_rate},null)"
            + " ,exchange=if(exchange is not null,income_amount*#{income_exchange_rate},null)"
            + ",income_exchange_rate=#{income_exchange_rate}"
            + " where   org_id=#{org_id} and  debit_note_id in (${debit_note_id}) and income_currency=#{income_currency}")
    void updateIncomeIO2(@Param("org_id") Integer org_id, @Param("debit_note_id") String debit_note_id, @Param("income_currency") String income_currency, @Param("income_exchange_rate") BigDecimal income_exchange_rate);


    @Update("update css_debit_note_currency set\n"
            + " functional_amount=amount*#{exchange_rate},exchange_rate=#{exchange_rate} \n"
            + " where   org_id=#{org_id} and  debit_note_id in (${debit_note_id}) and currency=#{income_currency}")
    void updateIncome3(@Param("org_id") Integer org_id, @Param("debit_note_id") String debit_note_id, @Param("income_currency") String income_currency, @Param("exchange_rate") BigDecimal exchange_rate);

    @Update("update css_debit_note set\n"
            + " functional_amount=#{functional_amount} \n"
            + " where   org_id=#{org_id} and debit_note_id =#{debit_note_id}")
    void updateIncome4(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id, @Param("functional_amount") BigDecimal functional_amount);

    @Select("SELECT debit_note_id,SUM(functional_amount) functional_amount \n" +
            " FROM css_debit_note_currency \n" +
            " WHERE org_id=#{org_id} AND debit_note_id in (${debit_note_id})" +
            " GROUP BY debit_note_id \n")
    List<CssDebitNote> getNoteList(@Param("org_id") Integer org_id, @Param("debit_note_id") String debit_note_id);

    @Options(statementType = StatementType.CALLABLE)
    @ResultMap({"result1", "result2"})
    @Select({"<script>",
            "CALL css_P_statement_print(#{org_id},#{businessScope},#{statementno},#{debit_note_language},#{userId})\n",
            "</script>"})
    List<List<StatementPrint>> printStatement(@Param("org_id") Integer org_id, @Param("businessScope") String businessScope, @Param("statementno") String statementno, @Param("debit_note_language") String debit_note_language, @Param("userId") Integer userId);

    @Select("SELECT\n" +
            "\tGROUP_CONCAT( debit_note_id ) AS debitNoteIds \n" +
            "FROM\n" +
            "\tcss_debit_note \n" +
            "WHERE\n" +
            "\tstatement_id = #{statementid} \n" +
            "\tAND org_id = #{org_id} \n" +
            "\tAND business_scope = #{businessScope}\n" +
            "GROUP BY\n" +
            "\tstatement_id \n")
    String getDebitNoteIds(@Param("statementid") String statementid, @Param("businessScope") String businessScope, @Param("org_id") Integer org_id);

    @Select({"<script>",
            "SELECT DISTINCT\n" +
                    "\to.servicer_id \n" +
                    "FROM\n" +
                    "\tcss_statement s\n" +
                    "\tINNER JOIN css_debit_note n ON s.statement_id = n.statement_id\n" +
                    "\tINNER JOIN af_order o ON n.order_id = o.order_id \n" +
                    "WHERE\n" +
                    "\ts.statement_id = #{statementId}",
            "</script>"})
    List<Integer> getServicerIdListByStatementId(@Param("statementId") Integer statementId);

    @Select({"<script>",
            "SELECT DISTINCT\n" +
                    "\to.servicer_id \n" +
                    "FROM\n" +
                    "\tcss_statement s\n" +
                    "\tINNER JOIN css_debit_note n ON s.statement_id = n.statement_id\n" +
                    "\tINNER JOIN sc_order o ON n.order_id = o.order_id \n" +
                    "WHERE\n" +
                    "\ts.statement_id = #{statementId}",
            "</script>"})
    List<Integer> getServicerIdListByStatementId1(@Param("statementId") Integer statementId);

    @Select({"<script>",
            "SELECT DISTINCT\n" +
                    "\to.servicer_id \n" +
                    "FROM\n" +
                    "\tcss_statement s\n" +
                    "\tINNER JOIN css_debit_note n ON s.statement_id = n.statement_id\n" +
                    "\tINNER JOIN tc_order o ON n.order_id = o.order_id \n" +
                    "WHERE\n" +
                    "\ts.statement_id = #{statementId}",
            "</script>"})
    List<Integer> getServicerIdListByStatementIdTC(@Param("statementId") Integer statementId);

    @Select({"<script>",
            "SELECT DISTINCT\n" +
                    "\to.servicer_id \n" +
                    "FROM\n" +
                    "\tcss_statement s\n" +
                    "\tINNER JOIN css_debit_note n ON s.statement_id = n.statement_id\n" +
                    "\tINNER JOIN lc_order o ON n.order_id = o.order_id \n" +
                    "WHERE\n" +
                    "\ts.statement_id = #{statementId}",
            "</script>"})
    List<Integer> getServicerIdListByStatementIdLC(@Param("statementId") Integer statementId);

    @Select({"<script>",
            "SELECT DISTINCT\n" +
                    "\to.servicer_id \n" +
                    "FROM\n" +
                    "\tcss_statement s\n" +
                    "\tINNER JOIN css_debit_note n ON s.statement_id = n.statement_id\n" +
                    "\tINNER JOIN io_order o ON n.order_id = o.order_id \n" +
                    "WHERE\n" +
                    "\ts.statement_id = #{statementId}",
            "</script>"})
    List<Integer> getServicerIdListByStatementIdIO(@Param("statementId") Integer statementId);

    @Select({"<script>",
            "SELECT DISTINCT\n" +
                    "\to.sales_id \n" +
                    "FROM\n" +
                    "\tcss_statement s\n" +
                    "\tINNER JOIN css_debit_note n ON s.statement_id = n.statement_id\n" +
                    "\tINNER JOIN af_order o ON n.order_id = o.order_id \n" +
                    "WHERE\n" +
                    "\ts.statement_id = #{statementId}",
            "</script>"})
    List<Integer> getSalesIdListByStatementId(@Param("statementId") Integer statementId);

    @Select({"<script>",
            "SELECT DISTINCT\n" +
                    "\to.sales_id \n" +
                    "FROM\n" +
                    "\tcss_statement s\n" +
                    "\tINNER JOIN css_debit_note n ON s.statement_id = n.statement_id\n" +
                    "\tINNER JOIN sc_order o ON n.order_id = o.order_id \n" +
                    "WHERE\n" +
                    "\ts.statement_id = #{statementId}",
            "</script>"})
    List<Integer> getSalesIdListByStatementId1(@Param("statementId") Integer statementId);

    @Select({"<script>",
            "SELECT DISTINCT\n" +
                    "\to.sales_id \n" +
                    "FROM\n" +
                    "\tcss_statement s\n" +
                    "\tINNER JOIN css_debit_note n ON s.statement_id = n.statement_id\n" +
                    "\tINNER JOIN tc_order o ON n.order_id = o.order_id \n" +
                    "WHERE\n" +
                    "\ts.statement_id = #{statementId}",
            "</script>"})
    List<Integer> getSalesIdListByStatementIdTC(@Param("statementId") Integer statementId);

    @Select({"<script>",
            "SELECT DISTINCT\n" +
                    "\to.sales_id \n" +
                    "FROM\n" +
                    "\tcss_statement s\n" +
                    "\tINNER JOIN css_debit_note n ON s.statement_id = n.statement_id\n" +
                    "\tINNER JOIN lc_order o ON n.order_id = o.order_id \n" +
                    "WHERE\n" +
                    "\ts.statement_id = #{statementId}",
            "</script>"})
    List<Integer> getSalesIdListByStatementIdLC(@Param("statementId") Integer statementId);

    @Select({"<script>",
            "SELECT DISTINCT\n" +
                    "\to.sales_id \n" +
                    "FROM\n" +
                    "\tcss_statement s\n" +
                    "\tINNER JOIN css_debit_note n ON s.statement_id = n.statement_id\n" +
                    "\tINNER JOIN io_order o ON n.order_id = o.order_id \n" +
                    "WHERE\n" +
                    "\ts.statement_id = #{statementId}",
            "</script>"})
    List<Integer> getSalesIdListByStatementIdIO(@Param("statementId") Integer statementId);
}
