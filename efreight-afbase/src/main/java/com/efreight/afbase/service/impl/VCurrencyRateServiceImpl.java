package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.afbase.entity.Currency;
import com.efreight.afbase.entity.VCurrencyRate;
import com.efreight.afbase.dao.VCurrencyRateMapper;
import com.efreight.afbase.service.CurrencyService;
import com.efreight.afbase.service.VCurrencyRateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * VIEW 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-12-09
 */
@Service
@AllArgsConstructor
public class VCurrencyRateServiceImpl extends ServiceImpl<VCurrencyRateMapper, VCurrencyRate> implements VCurrencyRateService {

    private final CurrencyService currencyService;

    @Override
    public List<VCurrencyRate> getList() {
        LambdaQueryWrapper<VCurrencyRate> wrapper = Wrappers.<VCurrencyRate>lambdaQuery();
        wrapper.eq(VCurrencyRate::getOrgId, SecurityUtils.getUser().getOrgId());
        List<VCurrencyRate> list = list(wrapper);
        list.stream().forEach(vCurrencyRate -> {
            if (StrUtil.isNotBlank(vCurrencyRate.getCurrencyCode())) {
                LambdaQueryWrapper<Currency> currencyWrapper = Wrappers.<Currency>lambdaQuery();
                currencyWrapper.eq(Currency::getCurrencyCode, vCurrencyRate.getCurrencyCode());
                Currency currency = currencyService.getOne(currencyWrapper);
                if (currency != null) {
                    vCurrencyRate.setCurrencyName(currency.getCurrencyName());
                    vCurrencyRate.setCurrencyNum(currency.getCurrencyNum());
                }
            }
        });
        return list;
    }

	@Override
	public VCurrencyRate getVCurrencyRateByCode(String currencyCode) {
		LambdaQueryWrapper<VCurrencyRate> wrapper = Wrappers.<VCurrencyRate>lambdaQuery();
        wrapper.eq(VCurrencyRate::getOrgId, SecurityUtils.getUser().getOrgId());
        wrapper.eq(VCurrencyRate::getCurrencyCode, currencyCode);
        List<VCurrencyRate> list = list(wrapper);
        if(list!=null&&list.size()>0) {
        	return list.get(0);
        }
        return null;
	}
}
