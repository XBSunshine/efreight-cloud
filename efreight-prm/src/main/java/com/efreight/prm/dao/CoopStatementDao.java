package com.efreight.prm.dao;

import com.efreight.prm.entity.statement.CoopStatement;
import com.efreight.prm.entity.statement.CoopStatementBean;
import com.efreight.prm.entity.statement.CoopStatementDetail;
import com.efreight.prm.entity.statement.CoopStatementQuery;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author lc
 * @date 2021/1/29 14:51
 */
public interface CoopStatementDao {

    @Select("CALL prm_P_report_receivable_age_EF(#{query.orgId}, #{query.overdue}, #{query.excess}, #{query.coopName} , #{query.billTemplate}, #{query.customerResponsible}," +
            " #{query.intervalAmount1},#{query.intervalAmount2},#{query.intervalAmount3},#{query.intervalAmount4},#{query.intervalAmount5})")
    List<CoopStatementBean> listCoopStatement(@Param("query") CoopStatementQuery query);

    @Select("select  " +
            " CONCAT(PC.coop_name, '(', PCS.statement_name, ')') AS statementName, " +
            " PCS.statement_status AS statementStatus," +
            " PCS.statement_date AS statementDate," +
            " FORMAT(IFNULL(PCS.amount_receivable, 0), 2) AS amountReceivable," +
            " FORMAT(IFNULL(PCS.amount_received, 0) - IFNULL(PCS.invoice_writeoff_amount, 0), 2) AS unverifiedAmount," +
            " CASE PCS.bill_template\n" +
            "            WHEN 'BJS' THEN '华北'\n" +
            "            WHEN 'CAN' THEN '华南'\n" +
            "            WHEN 'SHA' THEN '华东 '\n" +
            "            WHEN 'EFT' THEN '总部'\n" +
            "            ELSE '' END AS billTemplate," +
            " substring_index(IFNULL(PCS.confirm_saler_name, PCS.confirm_customer_name), ' ', 1) AS confirmSalerName," +
            " substring_index(PCS.confirm_customer_name, ' ', 1) AS confirmCustomerName," +
            " DATE_FORMAT(PCS.confirm_customer_time, '%Y-%m-%d %H:%i:%s') AS confirmCustomerTime," +
            " PCS.invoice_title AS invoiceTitle," +
            " PCS.invoice_type AS invoiceType," +
            " PCS.invoice_number AS invoiceNumber," +
            " substring_index(PCS.invoice_user_name, ' ', 1) AS invoiceUserName," +
            " DATE_FORMAT(PCS.invoice_date, '%Y-%m-%d') AS invoiceDate," +
            " PCS.invoice_writeoff_user_name AS invoiceWriteoffUserName," +
            " DATE_FORMAT(PCS.invoice_writeoff_date, '%Y-%m-%d') AS invoiceWriteoffDate," +
            " FORMAT(IFNULL(PCS.invoice_writeoff_amount, 0), 2) AS invoiceWriteoffAmount," +
            " PCS.express_number AS expressNumber," +
            " PCS.statement_mail_date AS sendBill," +
            " PCC.email AS invoiceMailTo "  +
            "FROM prm_coop_statement PCS LEFT JOIN prm_coop_agreement_settlement PCAS ON PCS.settlement_id = PCAS.settlement_id " +
            "LEFT JOIN prm_coop_contacts PCC ON PCS.invoice_mail_to = PCC.contacts_id " +
            "LEFT JOIN prm_coop PC ON PCS.coop_id = PC.coop_id " +
            "WHERE PCS.statement_status != '账单已核销' AND PCS.org_id = #{orgId} AND PCS.coop_id = #{coopId} " +
            "AND (CASE\n" +
            "\t\tPCS.bill_template\n" +
            "\t\tWHEN 'BJS' THEN\n" +
            "\t\t'华北' \n" +
            "\t\tWHEN 'CAN' THEN\n" +
            "\t\t'华南' \n" +
            "\t\tWHEN 'SHA' THEN\n" +
            "\t\t'华东 ' \n" +
            "\t\tWHEN 'EFT' THEN\n" +
            "\t\t'总部' ELSE '' \n" +
            "\tEND) = #{billTemplate}"+
            " ORDER BY PCS.statement_date ASC")
    List<CoopStatementDetail> listDetailCoopStatement(@Param("orgId") Integer orgId, @Param("coopId") Integer coopId, @Param("billTemplate") String billTemplate);

    CoopStatement selectById(Integer statementId);


    @Update("update prm_coop_statement set row_uuid=#{params.rowUuid}, invoice_writeoff_amount = #{params.invoiceWriteoffAmount},  " +
            " statement_status = #{params.statementStatus}, invoice_writeoff_user_name = #{params.invoiceWriteoffUserName}, invoice_writeoff_date = #{params.invoiceWriteoffDate}" +
            " where row_uuid = #{rowUuid}")
    int updateByRowUuid(@Param("params") CoopStatement statement, @Param("rowUuid") String rowUuid);
}
