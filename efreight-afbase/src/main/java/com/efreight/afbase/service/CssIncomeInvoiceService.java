package com.efreight.afbase.service;

import com.efreight.afbase.entity.CssIncomeInvoice;

import java.util.Map;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * CSS 应收：发票申请表 服务类
 * </p>
 *
 * @author cwd
 * @since 2020-12-28
 */
public interface CssIncomeInvoiceService extends IService<CssIncomeInvoice> {
	
	boolean doSave(CssIncomeInvoice bean);
	
	boolean cancelDNInvoice(Integer debitNoteId,String rowUuid);
	boolean cancelSTInvoice(Integer statementId,String rowUuid);
	
    IPage getPage(Page page,CssIncomeInvoice bean);
    
    Map openView(CssIncomeInvoice bean);
    
    CssIncomeInvoice invoiceView(Integer invoiceId);
    
    void exportExcelList(CssIncomeInvoice bean);

}
