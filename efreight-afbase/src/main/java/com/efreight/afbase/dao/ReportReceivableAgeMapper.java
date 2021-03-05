package com.efreight.afbase.dao;

import com.efreight.afbase.entity.exportExcel.ReportReceivableAgeDetail;
import com.efreight.afbase.entity.procedure.ReportReceivableAge;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.StatementType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface ReportReceivableAgeMapper {


    @Options(statementType = StatementType.CALLABLE)
    @ResultMap({"receivablerm1", "receivablerm2"})
    @Select("call css_P_report_receivable_age_AF (#{reportReceivableAge.orgId},#{reportReceivableAge.businessScope},#{reportReceivableAge.customerType},#{reportReceivableAge.customerName},#{reportReceivableAge.countRanges},#{reportReceivableAge.overdueValid},#{reportReceivableAge.durationValid},#{reportReceivableAge.otherOrg},#{reportReceivableAge.salesName},#{reportReceivableAge.orderPermission},#{reportReceivableAge.currentUserId})")
    List<List<LinkedHashMap<String, String>>> getPage(@Param("reportReceivableAge") ReportReceivableAge reportReceivableAge);

    @Options(statementType = StatementType.CALLABLE)
    @Select("call css_P_report_receivable_age_AF_detail (#{reportReceivableAge.orgId},#{reportReceivableAge.businessScope},#{reportReceivableAge.customerType},#{reportReceivableAge.customerId},#{reportReceivableAge.otherOrg},#{reportReceivableAge.salesName},#{reportReceivableAge.orderPermission},#{reportReceivableAge.currentUserId})")
    List<ReportReceivableAgeDetail> view(@Param("reportReceivableAge") ReportReceivableAge reportReceivableAge);

    @Options(statementType = StatementType.CALLABLE)
    @ResultMap({"receivablerm1", "receivablerm2"})
    @Select("call css_P_report_receivable_age_SC (#{reportReceivableAge.orgId},#{reportReceivableAge.businessScope},#{reportReceivableAge.customerType},#{reportReceivableAge.customerName},#{reportReceivableAge.countRanges},#{reportReceivableAge.overdueValid},#{reportReceivableAge.durationValid},#{reportReceivableAge.otherOrg},#{reportReceivableAge.salesName},#{reportReceivableAge.orderPermission},#{reportReceivableAge.currentUserId})")
    List<List<LinkedHashMap<String, String>>> getPageSC(@Param("reportReceivableAge") ReportReceivableAge reportReceivableAge);

    @Options(statementType = StatementType.CALLABLE)
    @Select("call css_P_report_receivable_age_SC_detail (#{reportReceivableAge.orgId},#{reportReceivableAge.businessScope},#{reportReceivableAge.customerType},#{reportReceivableAge.customerId},#{reportReceivableAge.otherOrg},#{reportReceivableAge.salesName},#{reportReceivableAge.orderPermission},#{reportReceivableAge.currentUserId})")
    List<ReportReceivableAgeDetail> viewSC(@Param("reportReceivableAge") ReportReceivableAge reportReceivableAge);

    @Options(statementType = StatementType.CALLABLE)
    @ResultMap({"receivablerm1", "receivablerm2"})
    @Select("call css_P_report_receivable_age_TC (#{reportReceivableAge.orgId},#{reportReceivableAge.businessScope},#{reportReceivableAge.customerType},#{reportReceivableAge.customerName},#{reportReceivableAge.countRanges},#{reportReceivableAge.overdueValid},#{reportReceivableAge.durationValid},#{reportReceivableAge.otherOrg},#{reportReceivableAge.salesName},#{reportReceivableAge.orderPermission},#{reportReceivableAge.currentUserId})")
    List<List<LinkedHashMap<String, String>>> getPageTC(@Param("reportReceivableAge") ReportReceivableAge reportReceivableAge);

    @Options(statementType = StatementType.CALLABLE)
    @Select("call css_P_report_receivable_age_TC_detail (#{reportReceivableAge.orgId},#{reportReceivableAge.businessScope},#{reportReceivableAge.customerType},#{reportReceivableAge.customerId},#{reportReceivableAge.otherOrg},#{reportReceivableAge.salesName},#{reportReceivableAge.orderPermission},#{reportReceivableAge.currentUserId})")
    List<ReportReceivableAgeDetail> viewTC(@Param("reportReceivableAge") ReportReceivableAge reportReceivableAge);

    @Options(statementType = StatementType.CALLABLE)
    @ResultMap({"receivablerm1", "receivablerm2"})
    @Select("call css_P_report_receivable_age_LC (#{reportReceivableAge.orgId},#{reportReceivableAge.businessScope},#{reportReceivableAge.customerType},#{reportReceivableAge.customerName},#{reportReceivableAge.countRanges},#{reportReceivableAge.overdueValid},#{reportReceivableAge.durationValid},#{reportReceivableAge.otherOrg},#{reportReceivableAge.salesName},#{reportReceivableAge.orderPermission},#{reportReceivableAge.currentUserId})")
    List<List<LinkedHashMap<String, String>>> getPageLC(@Param("reportReceivableAge") ReportReceivableAge reportReceivableAge);

    @Options(statementType = StatementType.CALLABLE)
    @Select("call css_P_report_receivable_age_LC_detail (#{reportReceivableAge.orgId},#{reportReceivableAge.businessScope},#{reportReceivableAge.customerType},#{reportReceivableAge.customerId},#{reportReceivableAge.otherOrg},#{reportReceivableAge.salesName},#{reportReceivableAge.orderPermission},#{reportReceivableAge.currentUserId})")
    List<ReportReceivableAgeDetail> viewLC(@Param("reportReceivableAge") ReportReceivableAge reportReceivableAge);

    @Options(statementType = StatementType.CALLABLE)
    @ResultMap({"receivablerm1", "receivablerm2"})
    @Select("call css_P_report_receivable_age_IO (#{reportReceivableAge.orgId},#{reportReceivableAge.businessScope},#{reportReceivableAge.customerType},#{reportReceivableAge.customerName},#{reportReceivableAge.countRanges},#{reportReceivableAge.overdueValid},#{reportReceivableAge.durationValid},#{reportReceivableAge.otherOrg},#{reportReceivableAge.salesName},#{reportReceivableAge.orderPermission},#{reportReceivableAge.currentUserId})")
    List<List<LinkedHashMap<String, String>>> getPageIO(@Param("reportReceivableAge") ReportReceivableAge reportReceivableAge);

    @Options(statementType = StatementType.CALLABLE)
    @Select("call css_P_report_receivable_age_IO_detail (#{reportReceivableAge.orgId},#{reportReceivableAge.businessScope},#{reportReceivableAge.customerType},#{reportReceivableAge.customerId},#{reportReceivableAge.otherOrg},#{reportReceivableAge.salesName},#{reportReceivableAge.orderPermission},#{reportReceivableAge.currentUserId})")
    List<ReportReceivableAgeDetail> viewIO(@Param("reportReceivableAge") ReportReceivableAge reportReceivableAge);
}
