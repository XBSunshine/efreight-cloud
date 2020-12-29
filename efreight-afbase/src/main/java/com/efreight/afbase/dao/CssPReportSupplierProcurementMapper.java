package com.efreight.afbase.dao;

import com.efreight.afbase.entity.CssPReportSupplierProcurement;
import com.efreight.afbase.entity.CssPReportSupplierProcurementDetail;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CssPReportSupplierProcurementMapper {

    @Select("CALL css_P_report_supplier_procurement_AF(#{cssPReportSupplierProcurement.statisticalPeriodStart},#{cssPReportSupplierProcurement.statisticalPeriodEnd},#{cssPReportSupplierProcurement.supplierType},#{cssPReportSupplierProcurement.statisticalPeriodType},#{cssPReportSupplierProcurement.isLock},#{cssPReportSupplierProcurement.goodsType},#{cssPReportSupplierProcurement.isAll},#{cssPReportSupplierProcurement.orgId},#{cssPReportSupplierProcurement.businessScope},#{cssPReportSupplierProcurement.otherOrg})")
    List<CssPReportSupplierProcurement> getAFList(@Param("cssPReportSupplierProcurement") CssPReportSupplierProcurement cssPReportSupplierProcurement);

    @Select("CALL css_P_report_supplier_procurement_AF_detail(#{cssPReportSupplierProcurementDetail.statisticalPeriodStart},#{cssPReportSupplierProcurementDetail.statisticalPeriodEnd},#{cssPReportSupplierProcurementDetail.statisticalPeriodType},#{cssPReportSupplierProcurementDetail.isLock},#{cssPReportSupplierProcurementDetail.goodsType},#{cssPReportSupplierProcurementDetail.orgId},#{cssPReportSupplierProcurementDetail.businessScope},#{cssPReportSupplierProcurementDetail.supplierId},#{cssPReportSupplierProcurementDetail.otherOrg})")
    List<CssPReportSupplierProcurementDetail> viewAFDetail(@Param("cssPReportSupplierProcurementDetail") CssPReportSupplierProcurementDetail cssPReportSupplierProcurementDetail);

    @Select("CALL css_P_report_supplier_procurement_SC(#{cssPReportSupplierProcurement.statisticalPeriodStart},#{cssPReportSupplierProcurement.statisticalPeriodEnd},#{cssPReportSupplierProcurement.supplierType},#{cssPReportSupplierProcurement.statisticalPeriodType},#{cssPReportSupplierProcurement.isLock},#{cssPReportSupplierProcurement.goodsType},#{cssPReportSupplierProcurement.isAll},#{cssPReportSupplierProcurement.orgId},#{cssPReportSupplierProcurement.businessScope},#{cssPReportSupplierProcurement.containerMethod},#{cssPReportSupplierProcurement.otherOrg})")
    List<CssPReportSupplierProcurement> getSCList(@Param("cssPReportSupplierProcurement") CssPReportSupplierProcurement cssPReportSupplierProcurement);

    @Select("CALL css_P_report_supplier_procurement_SC_detail(#{cssPReportSupplierProcurementDetail.statisticalPeriodStart},#{cssPReportSupplierProcurementDetail.statisticalPeriodEnd},#{cssPReportSupplierProcurementDetail.statisticalPeriodType},#{cssPReportSupplierProcurementDetail.isLock},#{cssPReportSupplierProcurementDetail.goodsType},#{cssPReportSupplierProcurementDetail.orgId},#{cssPReportSupplierProcurementDetail.businessScope},#{cssPReportSupplierProcurementDetail.supplierId},#{cssPReportSupplierProcurementDetail.containerMethod},#{cssPReportSupplierProcurementDetail.otherOrg})")
    List<CssPReportSupplierProcurementDetail> viewSCDetail(@Param("cssPReportSupplierProcurementDetail") CssPReportSupplierProcurementDetail cssPReportSupplierProcurementDetail);

    @Select("CALL css_P_report_supplier_procurement_TC(#{cssPReportSupplierProcurement.statisticalPeriodStart},#{cssPReportSupplierProcurement.statisticalPeriodEnd},#{cssPReportSupplierProcurement.supplierType},#{cssPReportSupplierProcurement.statisticalPeriodType},#{cssPReportSupplierProcurement.isLock},#{cssPReportSupplierProcurement.goodsType},#{cssPReportSupplierProcurement.isAll},#{cssPReportSupplierProcurement.orgId},#{cssPReportSupplierProcurement.businessScope},#{cssPReportSupplierProcurement.containerMethod},#{cssPReportSupplierProcurement.otherOrg})")
    List<CssPReportSupplierProcurement> getTCList(@Param("cssPReportSupplierProcurement") CssPReportSupplierProcurement cssPReportSupplierProcurement);

    @Select("CALL css_P_report_supplier_procurement_TC_detail(#{cssPReportSupplierProcurementDetail.statisticalPeriodStart},#{cssPReportSupplierProcurementDetail.statisticalPeriodEnd},#{cssPReportSupplierProcurementDetail.statisticalPeriodType},#{cssPReportSupplierProcurementDetail.isLock},#{cssPReportSupplierProcurementDetail.goodsType},#{cssPReportSupplierProcurementDetail.orgId},#{cssPReportSupplierProcurementDetail.businessScope},#{cssPReportSupplierProcurementDetail.supplierId},#{cssPReportSupplierProcurementDetail.containerMethod},#{cssPReportSupplierProcurementDetail.otherOrg})")
    List<CssPReportSupplierProcurementDetail> viewTCDetail(@Param("cssPReportSupplierProcurementDetail") CssPReportSupplierProcurementDetail cssPReportSupplierProcurementDetail);

    @Select("CALL css_P_report_supplier_procurement_LC(#{cssPReportSupplierProcurement.statisticalPeriodStart},#{cssPReportSupplierProcurement.statisticalPeriodEnd},#{cssPReportSupplierProcurement.supplierType},#{cssPReportSupplierProcurement.statisticalPeriodType},#{cssPReportSupplierProcurement.isLock},#{cssPReportSupplierProcurement.goodsType},#{cssPReportSupplierProcurement.isAll},#{cssPReportSupplierProcurement.orgId},#{cssPReportSupplierProcurement.otherOrg})")
    List<CssPReportSupplierProcurement> getLCList(@Param("cssPReportSupplierProcurement") CssPReportSupplierProcurement cssPReportSupplierProcurement);

    @Select("CALL css_P_report_supplier_procurement_LC_detail(#{cssPReportSupplierProcurementDetail.statisticalPeriodStart},#{cssPReportSupplierProcurementDetail.statisticalPeriodEnd},#{cssPReportSupplierProcurementDetail.statisticalPeriodType},#{cssPReportSupplierProcurementDetail.isLock},#{cssPReportSupplierProcurementDetail.goodsType},#{cssPReportSupplierProcurementDetail.orgId},#{cssPReportSupplierProcurementDetail.supplierId},#{cssPReportSupplierProcurementDetail.otherOrg})")
    List<CssPReportSupplierProcurementDetail> viewLCDetail(@Param("cssPReportSupplierProcurementDetail") CssPReportSupplierProcurementDetail cssPReportSupplierProcurementDetail);

    @Select("CALL css_P_report_supplier_procurement_IO(#{cssPReportSupplierProcurement.statisticalPeriodStart},#{cssPReportSupplierProcurement.statisticalPeriodEnd},#{cssPReportSupplierProcurement.supplierType},#{cssPReportSupplierProcurement.statisticalPeriodType},#{cssPReportSupplierProcurement.isLock},#{cssPReportSupplierProcurement.goodsType},#{cssPReportSupplierProcurement.isAll},#{cssPReportSupplierProcurement.orgId},#{cssPReportSupplierProcurement.otherOrg})")
    List<CssPReportSupplierProcurement> getIOList(@Param("cssPReportSupplierProcurement") CssPReportSupplierProcurement cssPReportSupplierProcurement);

    @Select("CALL css_P_report_supplier_procurement_IO_detail(#{cssPReportSupplierProcurementDetail.statisticalPeriodStart},#{cssPReportSupplierProcurementDetail.statisticalPeriodEnd},#{cssPReportSupplierProcurementDetail.statisticalPeriodType},#{cssPReportSupplierProcurementDetail.isLock},#{cssPReportSupplierProcurementDetail.goodsType},#{cssPReportSupplierProcurementDetail.orgId},#{cssPReportSupplierProcurementDetail.supplierId},#{cssPReportSupplierProcurementDetail.otherOrg})")
    List<CssPReportSupplierProcurementDetail> viewIODetail(@Param("cssPReportSupplierProcurementDetail") CssPReportSupplierProcurementDetail cssPReportSupplierProcurementDetail);

}
