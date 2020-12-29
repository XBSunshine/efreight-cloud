package com.efreight.afbase.service;

import com.efreight.afbase.entity.Currency;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.view.VCurrencyRate;

import java.util.List;

/**
 * <p>
 * 币种 服务类
 * </p>
 *
 * @author wangxx
 * @since 2019-07-17
 */
public interface CurrencyService extends IService<Currency> {

    List<VCurrencyRate> getCurrentListByOrgId();
}
