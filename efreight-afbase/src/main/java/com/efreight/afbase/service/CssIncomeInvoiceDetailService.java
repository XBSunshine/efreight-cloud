package com.efreight.afbase.service;

import com.efreight.afbase.entity.CssIncomeInvoiceDetail;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * CSS 应收：发票明细表 服务类
 * </p>
 *
 * @author cwd
 * @since 2020-12-28
 */
public interface CssIncomeInvoiceDetailService extends IService<CssIncomeInvoiceDetail> {
	
  boolean doSave(CssIncomeInvoiceDetail bean);
  boolean deleteInvoiceDetail(Integer invoiceDetailId,String rowUuid);
  CssIncomeInvoiceDetail invoiceDetailInfo(Integer invoiceDetailId);

}
