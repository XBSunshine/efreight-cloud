package com.efreight.afbase.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.efreight.afbase.entity.AfIncome;
import com.efreight.afbase.entity.CssDebitNoteCurrency;
import com.efreight.afbase.entity.StatementCurrency;
import com.efreight.afbase.dao.StatementCurrencyMapper;
import com.efreight.afbase.service.StatementCurrencyService;
import com.efreight.common.security.util.SecurityUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

/**
 * <p>
 * CSS 应收：清单 币种汇总表 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2019-12-26
 */
@Service
public class StatementCurrencyServiceImpl extends ServiceImpl<StatementCurrencyMapper, StatementCurrency> implements StatementCurrencyService {

	
	@Override
	public List<StatementCurrency> queryBill(String debitNoteIds) {
		List<StatementCurrency> billList = new ArrayList<StatementCurrency>();

		List<CssDebitNoteCurrency> beans=baseMapper.queryBill(SecurityUtils.getUser().getOrgId(),debitNoteIds);
		
		StatementCurrency bean = new StatementCurrency();
		bean.setAmount(beans.get(0).getAmount());
		bean.setCurrency(beans.get(0).getCurrency());
		BigDecimal currencyRate=baseMapper.getCurrencyRate(SecurityUtils.getUser().getOrgId(),bean.getCurrency());
		if (currencyRate!=null) {
			bean.setExchangeRate(currencyRate);
			bean.setFunctionalAmount(bean.getAmount().multiply(currencyRate));
		} else {
			bean.setExchangeRate(beans.get(0).getExchangeRate());
			bean.setFunctionalAmount(beans.get(0).getFunctionalAmount());
		}
		
		billList.add(bean);
		for (int i = 1; i < beans.size(); i++) {

			int isHaved=0;
			for (int j = 0; j < billList.size(); j++) {
				StatementCurrency getBean = billList.get(j);
				if (getBean.getCurrency().equals(beans.get(i).getCurrency())) {
					getBean.setAmount(getBean.getAmount().add(beans.get(i).getAmount()));
//					getBean.setFunctionalAmount(getBean.getFunctionalAmount().add(beans.get(i).getIncomeFunctionalAmount()));
					getBean.setFunctionalAmount(getBean.getAmount().multiply(getBean.getExchangeRate()));
					isHaved=1;
					break;
				} 
			}
			if (isHaved==0) {
				StatementCurrency bean2 = new StatementCurrency();
				bean2.setAmount(beans.get(i).getAmount());
				bean2.setCurrency(beans.get(i).getCurrency());
				BigDecimal currencyRate2=baseMapper.getCurrencyRate(SecurityUtils.getUser().getOrgId(),bean2.getCurrency());
				
				if (currencyRate2!=null) {
					bean2.setExchangeRate(currencyRate2);
					bean2.setFunctionalAmount(bean2.getAmount().multiply(currencyRate2));
				} else {
					bean2.setExchangeRate(beans.get(i).getExchangeRate());
					bean2.setFunctionalAmount(beans.get(i).getFunctionalAmount());
				}

				billList.add(bean2);
			}
		}
		return billList;
	}
	@Override
	public List<StatementCurrency> queryBillCurrency(Integer debitNoteId) {
		List<StatementCurrency> billList = baseMapper.queryBillCurrency(SecurityUtils.getUser().getOrgId(),debitNoteId);

		return billList;
	}
}
