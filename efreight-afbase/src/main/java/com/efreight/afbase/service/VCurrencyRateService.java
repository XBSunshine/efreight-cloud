package com.efreight.afbase.service;

import com.efreight.afbase.entity.VCurrencyRate;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * VIEW 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-12-09
 */
public interface VCurrencyRateService extends IService<VCurrencyRate> {

    List<VCurrencyRate> getList();
    VCurrencyRate getVCurrencyRateByCode(String currencyCode);
    
}
