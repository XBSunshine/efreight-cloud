package com.efreight.afbase.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.procedure.CssReportBusinessAnalysis;

public interface CssReportBusinessAnalysisService extends IService<CssReportBusinessAnalysis>{
	
	List<Map<String,String>> getListPage(CssReportBusinessAnalysis bean);
	List<Map<String,String>> getListPage2(CssReportBusinessAnalysis bean);
	List<Map<String,String>> getListPage22(CssReportBusinessAnalysis bean);
	List<Map<String,String>> getListPage23(CssReportBusinessAnalysis bean);
	List<Map<String,String>> getList3(CssReportBusinessAnalysis bean);
	List<Map<String,String>> getList4(CssReportBusinessAnalysis bean);
	List<Map<String,String>> getList5(CssReportBusinessAnalysis bean);
	
	

}
