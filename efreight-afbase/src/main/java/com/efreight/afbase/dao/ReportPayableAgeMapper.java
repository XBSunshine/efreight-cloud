package com.efreight.afbase.dao;

import com.efreight.afbase.entity.exportExcel.ReportPayableAgeDetail;
import com.efreight.afbase.entity.exportExcel.ReportPayableAgeDetailForExcel;
import com.efreight.afbase.entity.procedure.ReportPayableAge;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.StatementType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface ReportPayableAgeMapper {


    @Options(statementType = StatementType.CALLABLE)
    @ResultMap({"payableForAFrm1", "payableForAFrm2"})
    @Select("call css_P_report_payable_age_AF (#{reportPayableAge.orgId},#{reportPayableAge.businessScope},#{reportPayableAge.customerType},#{reportPayableAge.customerName},#{reportPayableAge.countRanges},#{reportPayableAge.otherOrg},#{reportPayableAge.salesName},#{reportPayableAge.orderPermission},#{reportPayableAge.currentUserId})")
    List<List<LinkedHashMap<String, String>>> getPageForAF(@Param("reportPayableAge") ReportPayableAge reportPayableAge);

    @Options(statementType = StatementType.CALLABLE)
    @Select("call css_P_report_payable_age_AF_detail (#{reportPayableAge.orgId},#{reportPayableAge.businessScope},#{reportPayableAge.customerId},#{reportPayableAge.otherOrg},#{reportPayableAge.salesName},#{reportPayableAge.orderPermission},#{reportPayableAge.currentUserId})")
    List<ReportPayableAgeDetail> viewForAF(@Param("reportPayableAge") ReportPayableAge reportPayableAge);

    @Options(statementType = StatementType.CALLABLE)
    @ResultMap({"payableForSCrm1", "payableForSCrm2"})
    @Select("call css_P_report_payable_age_SC (#{reportPayableAge.orgId},#{reportPayableAge.businessScope},#{reportPayableAge.customerType},#{reportPayableAge.customerName},#{reportPayableAge.countRanges},#{reportPayableAge.otherOrg},#{reportPayableAge.salesName},#{reportPayableAge.orderPermission},#{reportPayableAge.currentUserId})")
    List<List<LinkedHashMap<String, String>>> getPageForSC(@Param("reportPayableAge") ReportPayableAge reportPayableAge);

    @Options(statementType = StatementType.CALLABLE)
    @Select("call css_P_report_payable_age_SC_detail (#{reportPayableAge.orgId},#{reportPayableAge.businessScope},#{reportPayableAge.customerId},#{reportPayableAge.otherOrg},#{reportPayableAge.salesName},#{reportPayableAge.orderPermission},#{reportPayableAge.currentUserId})")
    List<ReportPayableAgeDetail> viewForSC(@Param("reportPayableAge") ReportPayableAge reportPayableAge);

    @Options(statementType = StatementType.CALLABLE)
    @ResultMap({"payableForSCrm1", "payableForSCrm2"})
    @Select("call css_P_report_payable_age_TC (#{reportPayableAge.orgId},#{reportPayableAge.businessScope},#{reportPayableAge.customerType},#{reportPayableAge.customerName},#{reportPayableAge.countRanges},#{reportPayableAge.otherOrg},#{reportPayableAge.salesName},#{reportPayableAge.orderPermission},#{reportPayableAge.currentUserId})")
    List<List<LinkedHashMap<String, String>>> getPageForTC(@Param("reportPayableAge") ReportPayableAge reportPayableAge);

    @Options(statementType = StatementType.CALLABLE)
    @Select("call css_P_report_payable_age_TC_detail (#{reportPayableAge.orgId},#{reportPayableAge.businessScope},#{reportPayableAge.customerId},#{reportPayableAge.otherOrg},#{reportPayableAge.salesName},#{reportPayableAge.orderPermission},#{reportPayableAge.currentUserId})")
    List<ReportPayableAgeDetail> viewForTC(@Param("reportPayableAge") ReportPayableAge reportPayableAge);

    @Options(statementType = StatementType.CALLABLE)
    @ResultMap({"payableForSCrm1", "payableForSCrm2"})
    @Select("call css_P_report_payable_age_LC (#{reportPayableAge.orgId},#{reportPayableAge.businessScope},#{reportPayableAge.customerType},#{reportPayableAge.customerName},#{reportPayableAge.countRanges},#{reportPayableAge.otherOrg},#{reportPayableAge.salesName},#{reportPayableAge.orderPermission},#{reportPayableAge.currentUserId})")
    List<List<LinkedHashMap<String, String>>> getPageForLC(@Param("reportPayableAge") ReportPayableAge reportPayableAge);

    @Options(statementType = StatementType.CALLABLE)
    @Select("call css_P_report_payable_age_LC_detail (#{reportPayableAge.orgId},#{reportPayableAge.businessScope},#{reportPayableAge.customerId},#{reportPayableAge.otherOrg},#{reportPayableAge.salesName},#{reportPayableAge.orderPermission},#{reportPayableAge.currentUserId})")
    List<ReportPayableAgeDetail> viewForLC(@Param("reportPayableAge") ReportPayableAge reportPayableAge);

    @Options(statementType = StatementType.CALLABLE)
    @ResultMap({"payableForSCrm1", "payableForSCrm2"})
    @Select("call css_P_report_payable_age_IO (#{reportPayableAge.orgId},#{reportPayableAge.businessScope},#{reportPayableAge.customerType},#{reportPayableAge.customerName},#{reportPayableAge.countRanges},#{reportPayableAge.otherOrg},#{reportPayableAge.salesName},#{reportPayableAge.orderPermission},#{reportPayableAge.currentUserId})")
    List<List<LinkedHashMap<String, String>>> getPageForIO(@Param("reportPayableAge") ReportPayableAge reportPayableAge);

    @Options(statementType = StatementType.CALLABLE)
    @Select("call css_P_report_payable_age_IO_detail (#{reportPayableAge.orgId},#{reportPayableAge.businessScope},#{reportPayableAge.customerId},#{reportPayableAge.otherOrg},#{reportPayableAge.salesName},#{reportPayableAge.orderPermission},#{reportPayableAge.currentUserId})")
    List<ReportPayableAgeDetail> viewForIO(@Param("reportPayableAge") ReportPayableAge reportPayableAge);
}
