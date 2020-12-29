package com.efreight.afbase.service;

import java.util.List;
import java.util.Map;

import com.efreight.afbase.entity.AfIncome;
import com.efreight.afbase.entity.CssDebitNoteCurrency;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * CSS 应收：账单 币种汇总表 服务类
 * </p>
 *
 * @author qipm
 * @since 2019-12-24
 */
public interface CssDebitNoteCurrencyService extends IService<CssDebitNoteCurrency> {

	List<CssDebitNoteCurrency> queryBill(List<AfIncome> beans);
	List<CssDebitNoteCurrency> queryBill2(String debitId,List<Map<String,Object>> beans);
}
