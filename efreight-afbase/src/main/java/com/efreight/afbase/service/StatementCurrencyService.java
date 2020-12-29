package com.efreight.afbase.service;

import java.util.List;

import com.efreight.afbase.entity.StatementCurrency;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * CSS 应收：清单 币种汇总表 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2019-12-26
 */
public interface StatementCurrencyService extends IService<StatementCurrency> {

	List<StatementCurrency> queryBill(String debitNoteIds);
	List<StatementCurrency> queryBillCurrency(Integer debitNoteId);
}
