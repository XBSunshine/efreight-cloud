package com.efreight.afbase.dao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.efreight.afbase.entity.AfIncome;
import com.efreight.afbase.entity.CssDebitNote;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 清单 Mapper 接口
 * </p>
 *
 * @author qipm
 * @since 2019-11-06
 */
public interface CssDebitNoteMapper extends BaseMapper<CssDebitNote> {

    @Select({"<script>",
            "select * from css_debit_note\n",
            "	where org_id = #{org_id} and debit_note_num like  \"%\"#{debit_note_num}\"%\"",
            " ORDER BY debit_note_id DESC ",
            "</script>"})
    List<CssDebitNote> selectCode(@Param("org_id") Integer org_id, @Param("debit_note_num") String debit_note_num);

    @Update("update af_income set\n"
            + " debit_note_id=#{debit_note_id} \n"
            + ",row_uuid=#{row_uuid} "
            + " where   org_id=#{org_id} and income_id in (${incomeIds}) ")
    void updateIncome(@Param("org_id") Integer org_id, @Param("incomeIds") String incomeIds, @Param("debit_note_id") Integer debit_note_id, @Param("financial_date") String financial_date, @Param("row_uuid") String row_uuid);

    @Update("update sc_income set\n"
            + " debit_note_id=#{debit_note_id} \n"
            + ",row_uuid=#{row_uuid} "
            + " where   org_id=#{org_id} and income_id in (${incomeIds}) ")
    void updateIncomeSE(@Param("org_id") Integer org_id, @Param("incomeIds") String incomeIds, @Param("debit_note_id") Integer debit_note_id, @Param("financial_date") String financial_date, @Param("row_uuid") String row_uuid);

    @Update("update tc_income set\n"
            + " debit_note_id=#{debit_note_id} \n"
            + ",row_uuid=#{row_uuid} "
            + " where   org_id=#{org_id} and income_id in (${incomeIds}) ")
    void updateIncomeTC(@Param("org_id") Integer org_id, @Param("incomeIds") String incomeIds, @Param("debit_note_id") Integer debit_note_id, @Param("financial_date") String financial_date, @Param("row_uuid") String row_uuid);

    @Update("update lc_income set\n"
            + " debit_note_id=#{debit_note_id} \n"
            + ",row_uuid=#{row_uuid} "
            + " where   org_id=#{org_id} and income_id in (${incomeIds}) ")
    void updateIncomeLC(@Param("org_id") Integer org_id, @Param("incomeIds") String incomeIds, @Param("debit_note_id") Integer debit_note_id, @Param("financial_date") String financial_date, @Param("row_uuid") String row_uuid);

    @Update("update io_income set\n"
            + " debit_note_id=#{debit_note_id} \n"
            + ",row_uuid=#{row_uuid} "
            + " where   org_id=#{org_id} and income_id in (${incomeIds}) ")
    void updateIncomeIO(@Param("org_id") Integer org_id, @Param("incomeIds") String incomeIds, @Param("debit_note_id") Integer debit_note_id, @Param("financial_date") String financial_date, @Param("row_uuid") String row_uuid);

    @Update("update af_income set\n"
            + " income_functional_amount=income_amount*#{income_exchange_rate} \n"
            + " ,main_routing=IF(main_routing IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,feeder=IF(feeder IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,operation=IF(operation IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,packaging=IF(packaging IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,storage=IF(storage IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,postage=IF(postage IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,clearance=IF(clearance IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,exchange=IF(exchange IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + ",income_exchange_rate=#{income_exchange_rate}"
            + " where   org_id=#{org_id} and debit_note_id=#{debit_note_id} and income_currency=#{income_currency}")
    void updateIncome2(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id, @Param("income_currency") String income_currency, @Param("income_exchange_rate") BigDecimal income_exchange_rate);

    @Update("update sc_income set\n"
            + " income_functional_amount=income_amount*#{income_exchange_rate}\n"
            + " ,main_routing=IF(main_routing IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,feeder=IF(feeder IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,operation=IF(operation IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,packaging=IF(packaging IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,storage=IF(storage IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,postage=IF(postage IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,clearance=IF(clearance IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,exchange=IF(exchange IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + ",income_exchange_rate=#{income_exchange_rate}"
            + " where   org_id=#{org_id} and debit_note_id=#{debit_note_id} and income_currency=#{income_currency}")
    void updateIncomeSE2(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id, @Param("income_currency") String income_currency, @Param("income_exchange_rate") BigDecimal income_exchange_rate);

    @Update("update tc_income set\n"
            + " income_functional_amount=income_amount*#{income_exchange_rate}\n"
            + " ,main_routing=IF(main_routing IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,feeder=IF(feeder IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,operation=IF(operation IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,packaging=IF(packaging IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,storage=IF(storage IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,postage=IF(postage IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,clearance=IF(clearance IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,exchange=IF(exchange IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + ",income_exchange_rate=#{income_exchange_rate}"
            + " where   org_id=#{org_id} and debit_note_id=#{debit_note_id} and income_currency=#{income_currency}")
    void updateIncomeTC2(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id, @Param("income_currency") String income_currency, @Param("income_exchange_rate") BigDecimal income_exchange_rate);

    @Update("update lc_income set\n"
            + " income_functional_amount=income_amount*#{income_exchange_rate}\n"
            + " ,main_routing=IF(main_routing IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,feeder=IF(feeder IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,operation=IF(operation IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,packaging=IF(packaging IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,storage=IF(storage IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,postage=IF(postage IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,clearance=IF(clearance IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,exchange=IF(exchange IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + ",income_exchange_rate=#{income_exchange_rate}"
            + " where   org_id=#{org_id} and debit_note_id=#{debit_note_id} and income_currency=#{income_currency}")
    void updateIncomeLC2(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id, @Param("income_currency") String income_currency, @Param("income_exchange_rate") BigDecimal income_exchange_rate);

    @Update("update io_income set\n"
            + " income_functional_amount=income_amount*#{income_exchange_rate}\n"
            + " ,main_routing=IF(main_routing IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,feeder=IF(feeder IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,operation=IF(operation IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,packaging=IF(packaging IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,storage=IF(storage IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,postage=IF(postage IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,clearance=IF(clearance IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + " ,exchange=IF(exchange IS NULL,NULL,income_amount*#{income_exchange_rate})"
            + ",income_exchange_rate=#{income_exchange_rate}"
            + " where   org_id=#{org_id} and debit_note_id=#{debit_note_id} and income_currency=#{income_currency}")
    void updateIncomeIO2(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id, @Param("income_currency") String income_currency, @Param("income_exchange_rate") BigDecimal income_exchange_rate);

    @Update("update af_order set "
            + " order_status='财务锁账' "
            + " where   org_id=#{org_id} and order_id=#{order_id} ")
    void updateOder(@Param("org_id") Integer org_id, @Param("order_id") Integer order_id);

    @Select({"<script>",
            "select * from css_debit_note A",
            " LEFT JOIN (SELECT debit_note_id",
            " ,GROUP_CONCAT(CONCAT('(',currency,')',FORMAT(amount,2)) separator ' ') AS currency_amount",
            " FROM css_debit_note_currency GROUP BY debit_note_id) B ON A.debit_note_id=B.debit_note_id",
            " where A.statement_id IS NULL AND A.invoice_id IS NULL  AND A.writeoff_complete IS NULL",
            " and A.order_uuid = #{bean.orderUuid}",
            " and A.customer_name = #{bean.customerName}",
//			" and currency = #{bean.currency}",
//            " and A.amount_tax_rate = #{bean.amountTaxRate}",
            " and A.org_id = #{bean.orgId}",
            "</script>"})
    List<CssDebitNote> queryHavedBill(@Param("bean") CssDebitNote bean);

    @Select({"<script>",
            "select * from af_income",
            "	where ",
            " debit_note_id = #{bean.debitNoteId}",
            " and org_id = #{bean.orgId}",
            "</script>"})
    List<AfIncome> queryHavedBillDetail(@Param("bean") AfIncome bean);

    @Select({"<script>",
            "select * from sc_income",
            "	where ",
            " debit_note_id = #{bean.debitNoteId}",
            " and org_id = #{bean.orgId}",
            "</script>"})
    List<AfIncome> queryHavedSEBillDetail(@Param("bean") AfIncome bean);

    @Select({"<script>",
            "select * from tc_income",
            "	where ",
            " debit_note_id = #{bean.debitNoteId}",
            " and org_id = #{bean.orgId}",
            "</script>"})
    List<AfIncome> queryHavedTCBillDetail(@Param("bean") AfIncome bean);

    @Select({"<script>",
            "select * from lc_income",
            "	where ",
            " debit_note_id = #{bean.debitNoteId}",
            " and org_id = #{bean.orgId}",
            "</script>"})
    List<AfIncome> queryHavedLCBillDetail(@Param("bean") AfIncome bean);

    @Select({"<script>",
            "select * from io_income",
            "	where ",
            " debit_note_id = #{bean.debitNoteId}",
            " and org_id = #{bean.orgId}",
            "</script>"})
    List<AfIncome> queryHavedIOBillDetail(@Param("bean") AfIncome bean);

    @Update("update css_debit_note set\n"
            + " invoice_num=#{invoice_num} \n"
            + " ,invoice_title=#{invoice_title} \n"
            + " ,invoice_date=#{invoice_date} \n"
            + " ,invoice_remark=#{invoice_remark} \n"
            + " where   org_id=#{org_id} and debit_note_id=#{debit_note_id} ")
    void doEditInvoiceRemark(@Param("org_id") Integer org_id, @Param("debit_note_id") Integer debit_note_id, @Param("invoice_remark") String invoice_remark, @Param("invoice_title") String invoice_title, @Param("invoice_num") String invoice_num, @Param("invoice_date") LocalDate invoice_date);

    @Update("update css_statement set\n"
            + " invoice_num=#{invoice_num} \n"
            + " ,invoice_title=#{invoice_title} \n"
            + " ,invoice_date=#{invoice_date} \n"
            + " ,invoice_remark=#{invoice_remark} \n"
            + " where   org_id=#{org_id} and statement_id=#{statement_id} ")
    void doEditInvoiceRemark2(@Param("org_id") Integer org_id, @Param("statement_id") Integer statement_id, @Param("invoice_remark") String invoice_remark, @Param("invoice_title") String invoice_title, @Param("invoice_num") String invoice_num, @Param("invoice_date") LocalDate invoice_date);
}
