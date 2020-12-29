package com.efreight.afbase.service;

import java.util.List;

import com.efreight.afbase.entity.CssDebitNoteCurrency;
import com.efreight.afbase.entity.CssIncomeWriteoff;
import com.efreight.afbase.entity.FinancialAccount;
import com.efreight.afbase.entity.StatementCurrency;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * CSS 应收：核销 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-02-03
 */
public interface CssIncomeWriteoffService extends IService<CssIncomeWriteoff> {

	IPage getPage(Page page, CssIncomeWriteoff bean);
	IPage getPage2(Page page, CssIncomeWriteoff bean);
	List<CssIncomeWriteoff> getTatol(CssIncomeWriteoff bean);
	
	List<CssDebitNoteCurrency> queryBillCurrency(Integer debitNoteId);
	List<StatementCurrency> queryListCurrency(Integer statementId);
	
	
	Boolean doBillWriteoff(CssIncomeWriteoff bean);
	Boolean doListWriteoff(CssIncomeWriteoff bean);
	Boolean doDeleteBillWriteoff(CssIncomeWriteoff bean);
	Boolean doDeleteListWriteoff(CssIncomeWriteoff bean);
	List<CssDebitNoteCurrency> queryBillDetail(Integer incomeWriteoffId);
	List<StatementCurrency> queryListDetail(Integer incomeWriteoffId);
	void exportExcelList(CssIncomeWriteoff bean);

	CssIncomeWriteoff getIncomeWriteoffById(Integer id);

	CssIncomeWriteoff getVoucherDate(Integer incomeWriteoffId);

	List<FinancialAccount> getFinancialAccount(String businessScope, Integer customerId);
}
