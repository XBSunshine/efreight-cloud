package com.efreight.afbase.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.CssPReportSettleAfExcel;
import com.efreight.afbase.entity.CssPReportSettleAfExcelAI;
import com.efreight.afbase.entity.CssPReportSettleAfExcelSE;
import com.efreight.afbase.entity.CssPReportSettleAfExcelSI;
import com.efreight.afbase.entity.CssPReportSettleExcel;
import com.efreight.afbase.entity.procedure.CssPReportSettleAfProcedure;

public interface CssPReportSettleAfService extends IService<CssPReportSettleAfProcedure>{
	
	HashMap getListPage(Page page, CssPReportSettleAfProcedure bean);
	
	List<CssPReportSettleAfExcel> getListForExcel(CssPReportSettleAfProcedure bean);
	List<CssPReportSettleAfExcelAI> getListForExcelAI(CssPReportSettleAfProcedure bean);
	List<CssPReportSettleAfExcelSE> getListForExcelSE(CssPReportSettleAfProcedure bean);
	List<CssPReportSettleAfExcelSI> getListForExcelSI(CssPReportSettleAfProcedure bean);
	List<CssPReportSettleExcel> getListForExcelNew(CssPReportSettleAfProcedure bean);

}
