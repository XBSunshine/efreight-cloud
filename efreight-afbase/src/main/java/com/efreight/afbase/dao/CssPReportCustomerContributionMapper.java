package com.efreight.afbase.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.efreight.afbase.entity.CssPReportCustomerContribution;
import com.efreight.afbase.entity.CustomerInContributionInfo;

public interface CssPReportCustomerContributionMapper {
	
	
	@Select("CALL css_P_report_customer_contribution_AF(#{bean.orgId},#{bean.businessScope},#{bean.startDate},#{bean.endDate},#{bean.businessProduct},#{bean.orderStatus},#{bean.goodsType},#{bean.isAllUser},#{bean.endDateYear},#{bean.countType},#{bean.otherOrg})")
	List<Map> getAfList(@Param("bean") CssPReportCustomerContribution bean);
	
	@Select("CALL css_P_report_customer_contribution_SC(#{bean.orgId},#{bean.businessScope},#{bean.startDate},#{bean.endDate},#{bean.containerMethod},#{bean.orderStatus},#{bean.goodsType},#{bean.isAllUser},#{bean.endDateYear},#{bean.countType},#{bean.otherOrg})")
	List<Map> getScList(@Param("bean") CssPReportCustomerContribution bean);
	@Select("CALL css_P_report_customer_contribution_TC(#{bean.orgId},#{bean.businessScope},#{bean.startDate},#{bean.endDate},#{bean.containerMethod},#{bean.orderStatus},#{bean.goodsType},#{bean.isAllUser},#{bean.endDateYear},#{bean.countType},#{bean.otherOrg})")
	List<Map> getTcList(@Param("bean") CssPReportCustomerContribution bean);
	@Select("CALL css_P_report_customer_contribution_LC(#{bean.orgId},#{bean.businessScope},#{bean.startDate},#{bean.endDate},#{bean.businessProduct},#{bean.orderStatus},#{bean.goodsType},#{bean.isAllUser},#{bean.endDateYear},#{bean.countType},#{bean.otherOrg})")
	List<Map> getLcList(@Param("bean") CssPReportCustomerContribution bean);
	@Select("CALL css_P_report_customer_contribution_IO(#{bean.orgId},#{bean.businessScope},#{bean.startDate},#{bean.endDate},#{bean.businessProduct},#{bean.orderStatus},#{bean.goodsType},#{bean.isAllUser},#{bean.endDateYear},#{bean.countType},#{bean.otherOrg})")
	List<Map> getIoList(@Param("bean") CssPReportCustomerContribution bean);
	
	@Select("CALL css_P_report_customer_contribution_AF_detail(#{bean.orgId},#{bean.businessScope},#{bean.startDate},#{bean.endDate},#{bean.businessProduct},#{bean.orderStatus},#{bean.goodsType},#{bean.endDateYear},#{bean.coopId},#{bean.countType},#{bean.dep},#{bean.arr},#{bean.coopType},#{bean.supplierName},#{bean.chooseRoutingNames},#{bean.country},#{bean.area},#{bean.otherOrg},#{bean.customerName})")
	List<Map> getAfListDetail(@Param("bean") CssPReportCustomerContribution bean);
	
	@Select("CALL css_P_report_customer_contribution_SC_detail(#{bean.orgId},#{bean.businessScope},#{bean.startDate},#{bean.endDate},#{bean.containerMethod},#{bean.orderStatus},#{bean.goodsType},#{bean.endDateYear},#{bean.coopId},#{bean.countType},#{bean.dep},#{bean.arr},#{bean.coopType},#{bean.supplierName},#{bean.chooseRoutingNames},#{bean.country},#{bean.otherOrg},#{bean.customerName})")
	List<Map> getScListDetail(@Param("bean") CssPReportCustomerContribution bean);
	
	@Select("CALL css_P_report_customer_contribution_TC_detail(#{bean.orgId},#{bean.businessScope},#{bean.startDate},#{bean.endDate},#{bean.containerMethod},#{bean.orderStatus},#{bean.goodsType},#{bean.endDateYear},#{bean.coopId},#{bean.countType},#{bean.dep},#{bean.arr},#{bean.coopType},#{bean.supplierName},#{bean.otherOrg},#{bean.customerName})")
	List<Map> getTcListDetail(@Param("bean") CssPReportCustomerContribution bean);
	@Select("CALL css_P_report_customer_contribution_LC_detail(#{bean.orgId},#{bean.businessScope},#{bean.startDate},#{bean.endDate},#{bean.businessProduct},#{bean.orderStatus},#{bean.goodsType},#{bean.endDateYear},#{bean.coopId},#{bean.countType},#{bean.dep},#{bean.arr},#{bean.coopType},#{bean.supplierName},#{bean.otherOrg},#{bean.customerName})")
	List<Map> getLcListDetail(@Param("bean") CssPReportCustomerContribution bean);
	@Select("CALL css_P_report_customer_contribution_IO_detail(#{bean.orgId},#{bean.businessScope},#{bean.startDate},#{bean.endDate},#{bean.businessProduct},#{bean.orderStatus},#{bean.goodsType},#{bean.endDateYear},#{bean.coopId},#{bean.countType},#{bean.dep},#{bean.arr},#{bean.coopType},#{bean.supplierName},#{bean.otherOrg},#{bean.customerName})")
	List<Map> getIoListDetail(@Param("bean") CssPReportCustomerContribution bean);
	
	@Select("CALL css_P_report_customer_contribution_AF_detail(#{bean.orgId},#{bean.businessScope},#{bean.startDate},#{bean.endDate},#{bean.businessProduct},#{bean.orderStatus},#{bean.goodsType},#{bean.endDateYear},#{bean.coopId},#{bean.countType},#{bean.dep},#{bean.arr},#{bean.coopType},#{bean.supplierName},#{bean.chooseRoutingNames},#{bean.country},#{bean.area},#{bean.otherOrg},#{bean.customerName})")
	List<CustomerInContributionInfo> getCustomerInfoAfDetail(@Param("bean") CssPReportCustomerContribution bean);
	
	@Select("CALL css_P_report_customer_contribution_SC_detail(#{bean.orgId},#{bean.businessScope},#{bean.startDate},#{bean.endDate},#{bean.containerMethod},#{bean.orderStatus},#{bean.goodsType},#{bean.endDateYear},#{bean.coopId},#{bean.countType},#{bean.dep},#{bean.arr},#{bean.coopType},#{bean.supplierName},#{bean.chooseRoutingNames},#{bean.country},#{bean.otherOrg},#{bean.customerName})")
	List<CustomerInContributionInfo> getCustomerInfoScDetail(@Param("bean") CssPReportCustomerContribution bean);
	
	@Select("CALL css_P_report_customer_contribution_TC_detail(#{bean.orgId},#{bean.businessScope},#{bean.startDate},#{bean.endDate},#{bean.containerMethod},#{bean.orderStatus},#{bean.goodsType},#{bean.endDateYear},#{bean.coopId},#{bean.countType},#{bean.dep},#{bean.arr},#{bean.coopType},#{bean.supplierName},#{bean.otherOrg},#{bean.customerName})")
	List<CustomerInContributionInfo> getCustomerInfoTcDetail(@Param("bean") CssPReportCustomerContribution bean);
	
	@Select("CALL css_P_report_customer_contribution_LC_detail(#{bean.orgId},#{bean.businessScope},#{bean.startDate},#{bean.endDate},#{bean.businessProduct},#{bean.orderStatus},#{bean.goodsType},#{bean.endDateYear},#{bean.coopId},#{bean.countType},#{bean.dep},#{bean.arr},#{bean.coopType},#{bean.supplierName},#{bean.otherOrg},#{bean.customerName})")
	List<CustomerInContributionInfo> getCustomerInfoLcDetail(@Param("bean") CssPReportCustomerContribution bean);
	@Select("CALL css_P_report_customer_contribution_IO_detail(#{bean.orgId},#{bean.businessScope},#{bean.startDate},#{bean.endDate},#{bean.businessProduct},#{bean.orderStatus},#{bean.goodsType},#{bean.endDateYear},#{bean.coopId},#{bean.countType},#{bean.dep},#{bean.arr},#{bean.coopType},#{bean.supplierName},#{bean.otherOrg},#{bean.customerName})")
	List<CustomerInContributionInfo> getCustomerInfoIoDetail(@Param("bean") CssPReportCustomerContribution bean);
	

}
