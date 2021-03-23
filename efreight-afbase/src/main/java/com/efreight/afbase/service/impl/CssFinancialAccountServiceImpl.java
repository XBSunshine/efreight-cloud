package com.efreight.afbase.service.impl;

import com.efreight.afbase.entity.CssFinancialAccount;
import com.efreight.afbase.dao.CssFinancialAccountMapper;
import com.efreight.afbase.entity.FinancialAccount;
import com.efreight.afbase.service.CssFinancialAccountService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.security.util.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * CSS 财务科目 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2021-01-13
 */
@Service
public class CssFinancialAccountServiceImpl extends ServiceImpl<CssFinancialAccountMapper, CssFinancialAccount> implements CssFinancialAccountService {

    @Override
    public List<FinancialAccount> getList(String businessScope) {
        return baseMapper.getList(businessScope, SecurityUtils.getUser().getOrgId());
    }
}
