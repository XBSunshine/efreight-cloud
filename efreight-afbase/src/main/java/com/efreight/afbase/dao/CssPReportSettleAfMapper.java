package com.efreight.afbase.dao;


import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.StatementType;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.CssPReportSettleAfExcel;
import com.efreight.afbase.entity.CssPReportSettleAfExcelAI;
import com.efreight.afbase.entity.CssPReportSettleAfExcelSE;
import com.efreight.afbase.entity.CssPReportSettleAfExcelSI;
import com.efreight.afbase.entity.CssPReportSettleExcel;
import com.efreight.afbase.entity.procedure.CssPReportSettleAfProcedure;

public interface CssPReportSettleAfMapper extends BaseMapper<CssPReportSettleAfProcedure> {


    @Options(statementType = StatementType.CALLABLE)
    @ResultMap({"rt1", "rt2"})
    @Select({"<script>",
            "CALL css_P_report_settle_AF(#{bean.orgId},#{bean.businessScope},#{bean.coopName},#{bean.awbFromType},#{bean.awbFromName},#{bean.departureStation},#{bean.arrivalStation},#{bean.transitStation},#{bean.businessProduct},#{bean.goodsType},#{bean.flightDateStart},#{bean.flightDateEnd},#{bean.financialDateStart},#{bean.financialDateEnd},#{bean.grossProfitStr},#{bean.awbNumber},#{bean.orderCode},#{bean.routingName},#{bean.salesName},#{bean.servicerName},#{bean.routingPersonName},#{bean.orderStatus},#{bean.expectFlight},#{bean.salesDep},#{bean.customerNumber},#{bean.goodsSourceCode},#{bean.orderPermission},#{bean.currentUserId},#{bean.reportType},#{bean.otherOrg})\n",
            "</script>"})
//    IPage<CssPReportSettleAfProcedure> getListPage(Page page, @Param("bean") CssPReportSettleAfProcedure bean);
    List<List<Map<String, String>>> getListPage(@Param("bean") CssPReportSettleAfProcedure bean);

    @Select({"<script>",
            "CALL css_P_report_settle_AF(#{bean.orgId},#{bean.businessScope},#{bean.coopName},#{bean.awbFromType},#{bean.awbFromName},#{bean.departureStation},#{bean.arrivalStation},#{bean.transitStation},#{bean.businessProduct},#{bean.goodsType},#{bean.flightDateStart},#{bean.flightDateEnd},#{bean.financialDateStart},#{bean.financialDateEnd},#{bean.grossProfitStr},#{bean.awbNumber},#{bean.orderCode},#{bean.routingName},#{bean.salesName},#{bean.servicerName},#{bean.routingPersonName},#{bean.orderStatus},#{bean.expectFlight},#{bean.salesDep},#{bean.customerNumber},#{bean.goodsSourceCode},#{bean.orderPermission},#{bean.currentUserId},#{bean.reportType},#{bean.otherOrg})\n",
            "</script>"})
    List<CssPReportSettleAfExcel> getListPageExcel(@Param("bean") CssPReportSettleAfProcedure bean);

    @Options(statementType = StatementType.CALLABLE)
    @ResultMap({"rt1", "rt2"})
    @Select({"<script>",
            "CALL css_P_report_settle_SC(#{bean.orgId},#{bean.businessScope},#{bean.coopName},#{bean.awbFromType},#{bean.awbFromName},#{bean.departureStation},#{bean.arrivalStation},#{bean.businessProduct},#{bean.goodsType},#{bean.flightDateStart},#{bean.flightDateEnd},#{bean.financialDateStart},#{bean.financialDateEnd},#{bean.grossProfitStr},#{bean.awbNumber},#{bean.orderCode},#{bean.routingName},#{bean.salesName},#{bean.servicerName},#{bean.orderStatus},#{bean.shipVoyageNumber},#{bean.salesDep},#{bean.customerNumber},#{bean.orderPermission},#{bean.currentUserId},#{bean.otherOrg})\n",
            "</script>"})
//    IPage<CssPReportSettleAfProcedure> getListPage(Page page, @Param("bean") CssPReportSettleAfProcedure bean);
    List<List<Map<String, String>>> getListPageSC(@Param("bean") CssPReportSettleAfProcedure bean);

    @Select({"<script>",
            "CALL css_P_report_settle_SC(#{bean.orgId},#{bean.businessScope},#{bean.coopName},#{bean.awbFromType},#{bean.awbFromName},#{bean.departureStation},#{bean.arrivalStation},#{bean.businessProduct},#{bean.goodsType},#{bean.flightDateStart},#{bean.flightDateEnd},#{bean.financialDateStart},#{bean.financialDateEnd},#{bean.grossProfitStr},#{bean.awbNumber},#{bean.orderCode},#{bean.routingName},#{bean.salesName},#{bean.servicerName},#{bean.orderStatus},#{bean.shipVoyageNumber},#{bean.salesDep},#{bean.otherOrg})\n",
            "</script>"})
    List<CssPReportSettleAfExcel> getListPageExcelSC(@Param("bean") CssPReportSettleAfProcedure bean);

    @Select({"<script>",
            "CALL css_P_report_settle_SC(#{bean.orgId},#{bean.businessScope},#{bean.coopName},#{bean.awbFromType},#{bean.awbFromName},#{bean.departureStation},#{bean.arrivalStation},#{bean.businessProduct},#{bean.goodsType},#{bean.flightDateStart},#{bean.flightDateEnd},#{bean.financialDateStart},#{bean.financialDateEnd},#{bean.grossProfitStr},#{bean.awbNumber},#{bean.orderCode},#{bean.routingName},#{bean.salesName},#{bean.servicerName},#{bean.orderStatus},#{bean.shipVoyageNumber},#{bean.salesDep},#{bean.customerNumber},#{bean.orderPermission},#{bean.currentUserId},#{bean.otherOrg})\n",
            "</script>"})
    List<CssPReportSettleAfExcelSE> getListPageExcelSE(@Param("bean") CssPReportSettleAfProcedure bean);

    @Select({"<script>",
            "CALL css_P_report_settle_SC(#{bean.orgId},#{bean.businessScope},#{bean.coopName},#{bean.awbFromType},#{bean.awbFromName},#{bean.departureStation},#{bean.arrivalStation},#{bean.businessProduct},#{bean.goodsType},#{bean.flightDateStart},#{bean.flightDateEnd},#{bean.financialDateStart},#{bean.financialDateEnd},#{bean.grossProfitStr},#{bean.awbNumber},#{bean.orderCode},#{bean.routingName},#{bean.salesName},#{bean.servicerName},#{bean.orderStatus},#{bean.shipVoyageNumber},#{bean.salesDep},#{bean.customerNumber},#{bean.orderPermission},#{bean.currentUserId},#{bean.otherOrg})\n",
            "</script>"})
    List<CssPReportSettleAfExcelSI> getListPageExcelSI(@Param("bean") CssPReportSettleAfProcedure bean);


    @Select({"<script>",
            "CALL css_P_report_settle_AF(#{bean.orgId},#{bean.businessScope},#{bean.coopName},#{bean.awbFromType},#{bean.awbFromName},#{bean.departureStation},#{bean.arrivalStation},#{bean.transitStation},#{bean.businessProduct},#{bean.goodsType},#{bean.flightDateStart},#{bean.flightDateEnd},#{bean.financialDateStart},#{bean.financialDateEnd},#{bean.grossProfitStr},#{bean.awbNumber},#{bean.orderCode},#{bean.routingName},#{bean.salesName},#{bean.servicerName},#{bean.routingPersonName},#{bean.orderStatus},#{bean.expectFlight},#{bean.salesDep},#{bean.customerNumber},#{bean.goodsSourceCode},#{bean.orderPermission},#{bean.currentUserId},#{bean.reportType},#{bean.otherOrg})\n",
            "</script>"})
    List<CssPReportSettleAfExcelAI> getListPageExcelAI(@Param("bean") CssPReportSettleAfProcedure bean);

    @Options(statementType = StatementType.CALLABLE)
    @ResultMap({"rt1", "rt2"})
    @Select({"<script>",
            "CALL css_P_report_settle_TC(#{bean.orgId},#{bean.businessScope},#{bean.coopName},#{bean.awbFromName},#{bean.departureStation},#{bean.arrivalStation},#{bean.exitPort},#{bean.goodsType},#{bean.flightDateStart},#{bean.flightDateEnd},#{bean.financialDateStart},#{bean.financialDateEnd},#{bean.grossProfitStr},#{bean.awbNumber},#{bean.orderCode},#{bean.salesName},#{bean.servicerName},#{bean.orderStatus},#{bean.salesDep},#{bean.customerNumber},#{bean.orderPermission},#{bean.currentUserId},#{bean.otherOrg})\n",
            "</script>"})
    List<List<Map<String, String>>> getListPageTC(@Param("bean") CssPReportSettleAfProcedure bean);

    @Options(statementType = StatementType.CALLABLE)
    @ResultMap({"rt1", "rt2"})
    @Select({"<script>",
            "CALL css_P_report_settle_LC(#{bean.orgId},#{bean.businessScope},#{bean.coopName},#{bean.departureStation},#{bean.arrivalStation},#{bean.shippingMethod },#{bean.flightDateStart},#{bean.flightDateEnd},#{bean.financialDateStart},#{bean.financialDateEnd},#{bean.grossProfitStr},#{bean.awbNumber},#{bean.orderCode},#{bean.salesName},#{bean.servicerName},#{bean.orderStatus},#{bean.salesDep},#{bean.customerNumber},#{bean.orderPermission},#{bean.currentUserId},#{bean.otherOrg})\n",
            "</script>"})
    List<List<Map<String, String>>> getListPageLC(@Param("bean") CssPReportSettleAfProcedure bean);

    @Options(statementType = StatementType.CALLABLE)
    @ResultMap({"rt1", "rt2"})
    @Select({"<script>",
            "CALL css_P_report_settle_IO(#{bean.orgId},#{bean.businessScope},#{bean.coopName},#{bean.departureStation},#{bean.arrivalStation},#{bean.businessMethod },#{bean.flightDateStart},#{bean.flightDateEnd},#{bean.financialDateStart},#{bean.financialDateEnd},#{bean.grossProfitStr},#{bean.awbNumber},#{bean.orderCode},#{bean.salesName},#{bean.servicerName},#{bean.orderStatus},#{bean.salesDep},#{bean.customerNumber},#{bean.orderPermission},#{bean.currentUserId},#{bean.otherOrg})\n",
            "</script>"})
    List<List<Map<String, String>>> getListPageIO(@Param("bean") CssPReportSettleAfProcedure bean);

    @Select({"<script>",
            "CALL css_P_report_settle_TC(#{bean.orgId},#{bean.businessScope},#{bean.coopName},#{bean.awbFromName},#{bean.departureStation},#{bean.arrivalStation},#{bean.exitPort},#{bean.goodsType},#{bean.flightDateStart},#{bean.flightDateEnd},#{bean.financialDateStart},#{bean.financialDateEnd},#{bean.grossProfitStr},#{bean.awbNumber},#{bean.orderCode},#{bean.salesName},#{bean.servicerName},#{bean.orderStatus},#{bean.salesDep},#{bean.customerNumber},#{bean.orderPermission},#{bean.currentUserId},#{bean.otherOrg})\n",
            "</script>"})
    List<CssPReportSettleExcel> getListPageTCExcel(@Param("bean") CssPReportSettleAfProcedure bean);

    @Select({"<script>",
            "CALL css_P_report_settle_LC(#{bean.orgId},#{bean.businessScope},#{bean.coopName},#{bean.departureStation},#{bean.arrivalStation},#{bean.businessMethod},#{bean.flightDateStart},#{bean.flightDateEnd},#{bean.financialDateStart},#{bean.financialDateEnd},#{bean.grossProfitStr},#{bean.awbNumber},#{bean.orderCode},#{bean.salesName},#{bean.servicerName},#{bean.orderStatus},#{bean.salesDep},#{bean.customerNumber},#{bean.orderPermission},#{bean.currentUserId},#{bean.otherOrg})\n",
            "</script>"})
    List<CssPReportSettleExcel> getListPageLCExcel(@Param("bean") CssPReportSettleAfProcedure bean);

    @Select({"<script>",
            "CALL css_P_report_settle_IO(#{bean.orgId},#{bean.businessScope},#{bean.coopName},#{bean.departureStation},#{bean.arrivalStation},#{bean.businessMethod},#{bean.flightDateStart},#{bean.flightDateEnd},#{bean.financialDateStart},#{bean.financialDateEnd},#{bean.grossProfitStr},#{bean.awbNumber},#{bean.orderCode},#{bean.salesName},#{bean.servicerName},#{bean.orderStatus},#{bean.salesDep},#{bean.customerNumber},#{bean.orderPermission},#{bean.currentUserId},#{bean.otherOrg})\n",
            "</script>"})
    List<CssPReportSettleExcel> getListPageIOExcel(@Param("bean") CssPReportSettleAfProcedure bean);

}
