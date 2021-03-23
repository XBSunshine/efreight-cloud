package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.afbase.entity.TariffDetails;
import com.efreight.afbase.entity.TariffDetailsCIQ;
import com.efreight.afbase.dao.TariffDetailsMapper;
import com.efreight.afbase.service.TariffDetailsService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

/**
 * <p>
 * AF 关税税则 服务实现类
 * </p>
 *
 * @author qipm
 * @since 2020-05-20
 */
@Service
public class TariffDetailsServiceImpl extends ServiceImpl<TariffDetailsMapper, TariffDetails> implements TariffDetailsService {

    @Override
    public IPage<TariffDetails> getListPage(Page page, TariffDetails bean) {

        return baseMapper.getListPage(page, bean);
    }

    @Override
    public List<TariffDetailsCIQ> getCIQ(TariffDetailsCIQ bean) {

        return baseMapper.getCIQ(bean);
    }

    @Override
    public List<TariffDetails> getList(String productName) {
        LambdaQueryWrapper<TariffDetails> wrapper = Wrappers.<TariffDetails>lambdaQuery();
        if (StrUtil.isNotBlank(productName)&&!productName.equals("null")) {
            wrapper.like(TariffDetails::getProductCode, productName).or().like(TariffDetails::getProductName, productName);
        }
        wrapper.orderByAsc(TariffDetails::getProductCode).last(" limit 10");
        return list(wrapper);
    }

    @Override
    public TariffDetails view(String productCode) {
        LambdaQueryWrapper<TariffDetails> wrapper = Wrappers.<TariffDetails>lambdaQuery();
        wrapper.eq(TariffDetails::getProductCode, productCode);
        return getOne(wrapper);
    }

    @Override
    public List<TariffDetails> getListForApi(String keyName) {

        LambdaQueryWrapper<TariffDetails> wrapper = Wrappers.<TariffDetails>lambdaQuery();
        if (StrUtil.isNotBlank(keyName)&&!keyName.equals("null")) {
            wrapper.like(TariffDetails::getProductCode, keyName).or().like(TariffDetails::getProductName, keyName);
        }
        return list(wrapper);
    }
}
