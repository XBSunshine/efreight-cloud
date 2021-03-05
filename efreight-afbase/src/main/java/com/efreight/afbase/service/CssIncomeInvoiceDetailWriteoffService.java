package com.efreight.afbase.service;

import com.efreight.afbase.entity.CssIncomeInvoiceDetailWriteoff;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * CSS 应收：发票明细 核销表 服务类
 * </p>
 *
 * @author cwd
 * @since 2020-12-28
 */
public interface CssIncomeInvoiceDetailWriteoffService extends IService<CssIncomeInvoiceDetailWriteoff> {
	
	boolean doSave(CssIncomeInvoiceDetailWriteoff bean);
	IPage getPage(Page page,CssIncomeInvoiceDetailWriteoff bean);
	void exportExcelList(CssIncomeInvoiceDetailWriteoff bean);
	
	CssIncomeInvoiceDetailWriteoff viewInfo(Integer invoiceDetailWriteoffId);
	
	boolean deleteInfo(Integer invoiceDetailWriteoffId);
	
	boolean updateAll(CssIncomeInvoiceDetailWriteoff bean);
	
	boolean invoiceAuto(CssIncomeInvoiceDetailWriteoff bean);

}
