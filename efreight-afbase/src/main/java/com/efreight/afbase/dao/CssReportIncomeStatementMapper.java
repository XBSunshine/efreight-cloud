package com.efreight.afbase.dao;

import com.efreight.afbase.entity.CssReportIncomeStatement;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface CssReportIncomeStatementMapper {

    @Select("CALL css_P_report_income_statement(#{cssReportIncomeStatement.orgId},#{cssReportIncomeStatement.start},#{cssReportIncomeStatement.end},#{cssReportIncomeStatement.voucherDateChecked},#{cssReportIncomeStatement.lockDateChecked},#{cssReportIncomeStatement.otherOrg})")
    List<Map<String, Object>> list(@Param("cssReportIncomeStatement") CssReportIncomeStatement cssReportIncomeStatement);
}
