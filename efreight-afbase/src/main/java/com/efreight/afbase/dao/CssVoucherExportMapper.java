package com.efreight.afbase.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.CssVoucherExport;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.StatementType;

import java.util.List;
import java.util.Map;

public interface CssVoucherExportMapper {


    @Select("<script>select expense_report_id,expense_financial_account_code,expense_financial_account_name,bank_financial_account_code,bank_financial_account_name,voucher_date,voucher_number,voucher_creator_name,expense_report_num,expense_report_status,expense_report_date,expenses_use,expense_amount,expense_report_remark,approval_financial_user_name,creator_name as expenseCreatorName from css_financial_expense_report" +
            " WHERE org_id=#{cssVoucherExport.orgId} and expense_report_status in ('已审核','已付款')" +
            "<when test='cssVoucherExport.paymentMethod!=null and cssVoucherExport.paymentMethod!=\"\"'>" +
            " and payment_method =#{cssVoucherExport.paymentMethod}" +
            "</when>" +
            "<when test='cssVoucherExport.expenseReportMode!=null and cssVoucherExport.expenseReportMode!=\"\"'>" +
            " and expense_report_mode =#{cssVoucherExport.expenseReportMode}" +
            "</when>" +
            "<when test='cssVoucherExport.expenseCreatorName!=null and cssVoucherExport.expenseCreatorName!=\"\"'>" +
            " and creator_name like \"%\"#{cssVoucherExport.expenseCreatorName}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.voucherCreatorName!=null and cssVoucherExport.voucherCreatorName!=\"\"'>" +
            " and voucher_creator_name like \"%\"#{cssVoucherExport.voucherCreatorName}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.expensesUse!=null and cssVoucherExport.expensesUse!=\"\"'>" +
            " and expenses_use like \"%\"#{cssVoucherExport.expensesUse}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.expenseReportNum!=null and cssVoucherExport.expenseReportNum!=\"\"'>" +
            " and expense_report_num like \"%\"#{cssVoucherExport.expenseReportNum}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.expenseReportDateStart!=null'>" +
            " and expense_report_date <![CDATA[>=]]> #{cssVoucherExport.expenseReportDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.expenseReportDateEnd!=null'>" +
            " and expense_report_date <![CDATA[<=]]> #{cssVoucherExport.expenseReportDateEnd}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateStart!=null'>" +
            " and voucher_date <![CDATA[>=]]> #{cssVoucherExport.voucherDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateEnd!=null'>" +
            " and voucher_date <![CDATA[<=]]> #{cssVoucherExport.voucherDateEnd}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==true'>" +
            " and voucher_date is not null" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==false'>" +
            " and voucher_date is null" +
            "</when>" +
            "order by expense_report_id desc</script>")
    IPage<CssVoucherExport> pageExpenseReport(Page page, @Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Select("<script>select expense_report_id,expense_financial_account_code,expense_financial_account_name,bank_financial_account_code,bank_financial_account_name,voucher_date,voucher_number,voucher_creator_name,expense_report_num,expense_report_status,expense_report_date,expenses_use,expense_amount,expense_report_remark,approval_financial_user_name,creator_name as expenseCreatorName from css_financial_expense_report" +
            " WHERE org_id=#{cssVoucherExport.orgId} and expense_report_status in ('已审核','已付款')" +
            "<when test='cssVoucherExport.paymentMethod!=null and cssVoucherExport.paymentMethod!=\"\"'>" +
            " and payment_method =#{cssVoucherExport.paymentMethod}" +
            "</when>" +
            "<when test='cssVoucherExport.expenseReportMode!=null and cssVoucherExport.expenseReportMode!=\"\"'>" +
            " and expense_report_mode =#{cssVoucherExport.expenseReportMode}" +
            "</when>" +
            "<when test='cssVoucherExport.expenseCreatorName!=null and cssVoucherExport.expenseCreatorName!=\"\"'>" +
            " and creator_name like \"%\"#{cssVoucherExport.expenseCreatorName}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.voucherCreatorName!=null and cssVoucherExport.voucherCreatorName!=\"\"'>" +
            " and voucher_creator_name like \"%\"#{cssVoucherExport.voucherCreatorName}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.expensesUse!=null and cssVoucherExport.expensesUse!=\"\"'>" +
            " and expenses_use like \"%\"#{cssVoucherExport.expensesUse}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.expenseReportNum!=null and cssVoucherExport.expenseReportNum!=\"\"'>" +
            " and expense_report_num like \"%\"#{cssVoucherExport.expenseReportNum}\"%\"" +
            "</when>" +
            "<when test='cssVoucherExport.expenseReportDateStart!=null'>" +
            " and expense_report_date <![CDATA[>=]]> #{cssVoucherExport.expenseReportDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.expenseReportDateEnd!=null'>" +
            " and expense_report_date <![CDATA[<=]]> #{cssVoucherExport.expenseReportDateEnd}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateStart!=null'>" +
            " and voucher_date <![CDATA[>=]]> #{cssVoucherExport.voucherDateStart}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherDateEnd!=null'>" +
            " and voucher_date <![CDATA[<=]]> #{cssVoucherExport.voucherDateEnd}" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==true'>" +
            " and voucher_date is not null" +
            "</when>" +
            "<when test='cssVoucherExport.voucherStatus!=null and cssVoucherExport.voucherStatus==false'>" +
            " and voucher_date is null" +
            "</when>" +
            "order by expense_report_id desc</script>")
    List<CssVoucherExport> listExpenseReport(@Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Select({"<script>",
            "CALL css_P_financial_expense_report_print(#{cssVoucherExport.orgId},2,#{cssVoucherExport.sql},null,null,null,#{cssVoucherExport.voucherCreatorId},#{cssVoucherExport.voucherCreatorName})\n",
            "</script>"})
    Map<String, String> voucherCallbackForExpenseReport(@Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Options(statementType = StatementType.CALLABLE)

    @ResultMap({"voucherExport1", "voucherExport2"})
    @Select({"<script>",
            "CALL css_P_financial_expense_report_print(#{cssVoucherExport.orgId},1,#{cssVoucherExport.sql},#{cssVoucherExport.voucherDate},#{cssVoucherExport.voucherNumber},#{cssVoucherExport.voucherIsDetail},#{cssVoucherExport.voucherCreatorId},#{cssVoucherExport.voucherCreatorName})\n",
            "</script>"})
    List voucherGenerateForExpenseReport(@Param("cssVoucherExport") CssVoucherExport cssVoucherExport);

    @Select("SELECT MAX(voucher_number) AS voucher_number FROM css_financial_voucher_number_log\n" +
            "WHERE org_id=#{orgId}\n" +
            "AND return_voucher_creator_id IS NULL\n" +
            "AND DATE_FORMAT(voucher_date,'%Y-%m')=#{voucherDate}")
    Integer getMaxVoucherNumber(@Param("voucherDate") String voucherDate, @Param("orgId") Integer orgId);

    @Select("select count(1) from css_financial_voucher_number_log where org_id=#{orgId} and DATE_FORMAT(voucher_date,'%Y-%m')=#{voucherDate} and return_voucher_creator_id IS NULL and voucher_number in (${voucherNumber})")
    int checkVoucherNumber(@Param("voucherDate") String voucherDate, @Param("voucherNumber") String voucherNumber, @Param("orgId") Integer orgId);
}
