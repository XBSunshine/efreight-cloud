package com.efreight.afbase.service;

import java.util.List;
import java.util.Map;

import com.efreight.afbase.entity.CssPReportCustomerContribution;

public interface CssPReportCustomerContributionService {
	
	List<Map> getAfList(CssPReportCustomerContribution bean);
	List<Map> getScList(CssPReportCustomerContribution bean);
	List<Map> getAfListDetail(CssPReportCustomerContribution bean);
	List<Map> getScListDetail(CssPReportCustomerContribution bean);
	
	Map getCustomerDetail(CssPReportCustomerContribution bean);

}
