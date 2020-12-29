package com.efreight.afbase.service.impl;

import com.efreight.afbase.entity.Currency;
import com.efreight.afbase.dao.CurrencyMapper;
import com.efreight.afbase.entity.view.VCurrencyRate;
import com.efreight.afbase.service.CurrencyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.security.util.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 币种 服务实现类
 * </p>
 *
 * @author wangxx
 * @since 2019-07-17
 */
@Service
public class CurrencyServiceImpl extends ServiceImpl<CurrencyMapper, Currency> implements CurrencyService {

    @Override
    public List<VCurrencyRate> getCurrentListByOrgId() {
        List<VCurrencyRate> list = baseMapper.getCurrentListByOrgId(SecurityUtils.getUser().getOrgId());
        return list;
    }
}
