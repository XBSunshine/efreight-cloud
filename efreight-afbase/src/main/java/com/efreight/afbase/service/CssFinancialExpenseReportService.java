package com.efreight.afbase.service;

import com.efreight.afbase.entity.CssFinancialExpenseReport;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * CSS 财务费用报销 服务类
 * </p>
 *
 * @author caiwd
 * @since 2020-10-14
 */
public interface CssFinancialExpenseReportService extends IService<CssFinancialExpenseReport> {
	
	IPage<CssFinancialExpenseReport> getListPage(Page page, CssFinancialExpenseReport bean);
	Map getDeptInfo();
	List<Map> getOrguser();
	Map getSubject();
	CssFinancialExpenseReport doSave(CssFinancialExpenseReport bean); 
	CssFinancialExpenseReport modify(CssFinancialExpenseReport bean); 
	CssFinancialExpenseReport modifyStatus(CssFinancialExpenseReport bean); 
	CssFinancialExpenseReport audit(CssFinancialExpenseReport bean);
	CssFinancialExpenseReport payment(CssFinancialExpenseReport bean);
	void delete(CssFinancialExpenseReport bean);
	void print(CssFinancialExpenseReport bean);
	
	CssFinancialExpenseReport view(Integer expenseReportId);
}
