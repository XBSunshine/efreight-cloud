package com.efreight.afbase.service;

import java.util.List;

import com.efreight.afbase.entity.AfIncome;
import com.efreight.afbase.entity.CssDebitNote;
import com.efreight.afbase.entity.DebitNote;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 清单 服务类
 * </p>
 *
 * @author qipm
 * @since 2019-11-06
 */
public interface CssDebitNoteService extends IService<CssDebitNote> {
	Boolean doEditInvoiceRemark(DebitNote bean);
	Boolean doEditInvoiceRemark2(DebitNote bean);
	Boolean doSave(CssDebitNote bean);
	Boolean doUpdate(CssDebitNote bean);
	
	List<CssDebitNote> queryHavedBill(CssDebitNote bean);
	List<AfIncome> queryHavedBillDetail(AfIncome bean);

}
