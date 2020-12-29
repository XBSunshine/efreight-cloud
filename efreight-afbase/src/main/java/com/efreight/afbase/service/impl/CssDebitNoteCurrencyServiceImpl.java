package com.efreight.afbase.service.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.efreight.afbase.entity.AfIncome;
import com.efreight.afbase.entity.CssDebitNoteCurrency;
import com.efreight.afbase.dao.CssDebitNoteCurrencyMapper;
import com.efreight.afbase.service.CssDebitNoteCurrencyService;
import com.efreight.common.security.util.SecurityUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

/**
 * <p>
 * CSS 应收：账单 币种汇总表 服务实现类
 * </p>
 *
 * @author qipm
 * @since 2019-12-24
 */
@Service
public class CssDebitNoteCurrencyServiceImpl extends
		ServiceImpl<CssDebitNoteCurrencyMapper, CssDebitNoteCurrency> implements
		CssDebitNoteCurrencyService {

	@Override
	public List<CssDebitNoteCurrency> queryBill(List<AfIncome> beans) {
		List<CssDebitNoteCurrency> billList = new ArrayList<CssDebitNoteCurrency>();

		
		CssDebitNoteCurrency bean = new CssDebitNoteCurrency();
		bean.setAmount(beans.get(0).getIncomeAmount());
		bean.setCurrency(beans.get(0).getIncomeCurrency());
		BigDecimal currencyRate=baseMapper.getCurrencyRate(SecurityUtils.getUser().getOrgId(),bean.getCurrency());
		if (currencyRate!=null) {
			bean.setExchangeRate(currencyRate);
			bean.setFunctionalAmount(bean.getAmount().multiply(currencyRate));
		} else {
			bean.setExchangeRate(beans.get(0).getIncomeExchangeRate());
			bean.setFunctionalAmount(beans.get(0).getIncomeFunctionalAmount());
		}
		
		billList.add(bean);
		for (int i = 1; i < beans.size(); i++) {

			int isHaved=0;
			for (int j = 0; j < billList.size(); j++) {
				CssDebitNoteCurrency getBean = billList.get(j);
				if (getBean.getCurrency().equals(beans.get(i).getIncomeCurrency())) {
					getBean.setAmount(getBean.getAmount().add(beans.get(i).getIncomeAmount()));
//					getBean.setFunctionalAmount(getBean.getFunctionalAmount().add(beans.get(i).getIncomeFunctionalAmount()));
					getBean.setFunctionalAmount(getBean.getAmount().multiply(getBean.getExchangeRate()));
					isHaved=1;
					break;
				} 
			}
			if (isHaved==0) {
				CssDebitNoteCurrency bean2 = new CssDebitNoteCurrency();
				bean2.setAmount(beans.get(i).getIncomeAmount());
				bean2.setCurrency(beans.get(i).getIncomeCurrency());
				BigDecimal currencyRate2=baseMapper.getCurrencyRate(SecurityUtils.getUser().getOrgId(),bean2.getCurrency());
				
				if (currencyRate2!=null) {
					bean2.setExchangeRate(currencyRate2);
					bean2.setFunctionalAmount(bean2.getAmount().multiply(currencyRate2));
				} else {
					bean2.setExchangeRate(beans.get(i).getIncomeExchangeRate());
					bean2.setFunctionalAmount(beans.get(i).getIncomeFunctionalAmount());
				}

				billList.add(bean2);
			}
		}
		return billList;
	}
	@Override
	public List<CssDebitNoteCurrency> queryBill2(String debitId,List<Map<String,Object>> beans) {
		List<CssDebitNoteCurrency> billList = baseMapper.queryBill2(SecurityUtils.getUser().getOrgId(),debitId);
		
		int start=1;
		if (billList.size()==0) {
			CssDebitNoteCurrency bean = new CssDebitNoteCurrency();
			bean.setAmount(getBigDecimal(beans.get(0).get("incomeAmount")));
			bean.setCurrency((String) beans.get(0).get("incomeCurrency"));
			BigDecimal currencyRate=baseMapper.getCurrencyRate(SecurityUtils.getUser().getOrgId(),bean.getCurrency());

			if (currencyRate!=null) {
				bean.setExchangeRate(currencyRate);
				bean.setFunctionalAmount(bean.getAmount().multiply(currencyRate));
			} else {
				bean.setExchangeRate(getBigDecimal(beans.get(0).get("incomeExchangeRate")));
				bean.setFunctionalAmount(getBigDecimal( beans.get(0).get("incomeFunctionalAmount")));
			}
			billList.add(bean);
		} else {
			start=0;
		}
		
		for (int i = start; i < beans.size(); i++) {
			int isHaved=0;
			for (int j = 0; j < billList.size(); j++) {
				CssDebitNoteCurrency getBean = billList.get(j);
				if (getBean.getCurrency().equals((String) beans.get(i).get("incomeCurrency"))) {
					getBean.setAmount(getBean.getAmount().add(getBigDecimal(beans.get(i).get("incomeAmount"))));
//					getBean.setFunctionalAmount(getBean.getFunctionalAmount().add(getBigDecimal(beans.get(i).get("incomeFunctionalAmount"))));
					getBean.setFunctionalAmount(getBean.getAmount().multiply(getBean.getExchangeRate()));
					isHaved=1;
					break;
				} 
			}
			if (isHaved==0) {
				CssDebitNoteCurrency bean2 = new CssDebitNoteCurrency();
				bean2.setAmount(getBigDecimal(beans.get(i).get("incomeAmount")));
				bean2.setCurrency((String) beans.get(i).get("incomeCurrency"));
				BigDecimal currencyRate2=baseMapper.getCurrencyRate(SecurityUtils.getUser().getOrgId(),bean2.getCurrency());
				
				if (currencyRate2!=null) {
					bean2.setExchangeRate(currencyRate2);
					bean2.setFunctionalAmount(bean2.getAmount().multiply(currencyRate2));
				} else {
					bean2.setExchangeRate(getBigDecimal(beans.get(i).get("incomeExchangeRate")));
					bean2.setFunctionalAmount(getBigDecimal(beans.get(i).get("incomeFunctionalAmount")));
				}
				
				billList.add(bean2);
			}
		}
		return billList;
	}
	public  BigDecimal getBigDecimal( Object value ) {  
        BigDecimal ret = null;  
        if( value != null ) {  
            if( value instanceof BigDecimal ) {  
                ret = (BigDecimal) value;  
            } else if( value instanceof String ) {  
                ret = new BigDecimal( (String) value );  
            } else if( value instanceof BigInteger ) {  
                ret = new BigDecimal( (BigInteger) value );  
            } else if( value instanceof Number ) {  
                ret = new BigDecimal( ((Number)value).doubleValue() );  
            } else {  
                throw new ClassCastException("Not possible to coerce ["+value+"] from class "+value.getClass()+" into a BigDecimal.");  
            }  
        }  
        return ret;  
    }
}
