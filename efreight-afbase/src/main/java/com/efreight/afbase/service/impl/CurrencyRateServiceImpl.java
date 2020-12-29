/*
 *
 * Author:zhanghw
 */
package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.CurrencyRateMapper;
import com.efreight.afbase.entity.Currency;
import com.efreight.afbase.entity.CurrencyRate;
import com.efreight.afbase.service.CurrencyRateService;
import com.efreight.common.security.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 汇率维护
 *
 * @author zhanghw
 * @date 2019-08-30 17:42:34
 */
@Service
public class CurrencyRateServiceImpl extends ServiceImpl<CurrencyRateMapper, CurrencyRate> implements CurrencyRateService {

    @Override
    public IPage getPage(Page page, CurrencyRate currencyRate) {
        QueryWrapper<CurrencyRate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("org_id", SecurityUtils.getUser().getOrgId());
        if (StrUtil.isNotBlank(currencyRate.getCurrencyCode())) {
            queryWrapper.and(wrapper -> wrapper.like("currency_code", currencyRate.getCurrencyCode()));
        }
        if (currencyRate.getCreateTime() != null) {
            queryWrapper.and(wrapper1 -> wrapper1.le("begin_date", currencyRate.getCreateTime()).ge("end_date", currencyRate.getCreateTime()).or(wrapper2 -> wrapper2.le("begin_date", currencyRate.getCreateTime()).isNull("end_date")));
        }
        queryWrapper.orderByAsc("currency_code", "begin_date");
        return baseMapper.selectPage(page, queryWrapper);
    }

    @Override
    public CurrencyRate lastOperation(String currencyCode) {
        QueryWrapper<CurrencyRate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("org_id", SecurityUtils.getUser().getOrgId());
        queryWrapper.eq("currency_code", currencyCode);
        queryWrapper.orderByDesc("cr_id");
        List<CurrencyRate> currencyRates = baseMapper.selectList(queryWrapper);
        if (currencyRates == null || currencyRates.size() == 0) {
            return null;
        } else {
            return currencyRates.get(0);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insert(CurrencyRate currencyRate) {
        Integer crId = currencyRate.getCrId();
        currencyRate.setOrgId(SecurityUtils.getUser().getOrgId());
        currencyRate.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        currencyRate.setCreateTime(LocalDateTime.now());
        currencyRate.setCreatorId(SecurityUtils.getUser().getId());
        currencyRate.setCrId(null);
        baseMapper.insert(currencyRate);
        if (crId != null) {
            CurrencyRate old = baseMapper.selectById(crId);
            if (old != null) {
                old.setEndDate(currencyRate.getBeginDate().minusSeconds(1));
//                .minus(1, ChronoUnit.DAYS)
                baseMapper.updateById(old);
            }
        }
    }

    @Override
    public Currency getCurrencyByCode(String currencyCode) {
        Currency currency = baseMapper.getCurrencyByCode(currencyCode);
        if (currency == null) {
            return null;
        } else {
            return currency;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remoteInsert(CurrencyRate currencyRate) {
        currencyRate.setEndDate(null);
        baseMapper.insert(currencyRate);
    }
}
