/*
 *
 * Author: zhanghw
 */

package com.efreight.afbase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.Currency;
import com.efreight.afbase.entity.CurrencyRate;

/**
 * 汇率维护
 *
 * @author zhanghw
 * @date 2019-08-30 17:42:34
 */
public interface CurrencyRateService extends IService<CurrencyRate> {

    IPage getPage(Page page, CurrencyRate currencyRate);

    CurrencyRate lastOperation(String currencyCode);

    void insert(CurrencyRate currencyRate);

    Currency getCurrencyByCode(String currencyCode);

    void remoteInsert(CurrencyRate currencyRate);
}
