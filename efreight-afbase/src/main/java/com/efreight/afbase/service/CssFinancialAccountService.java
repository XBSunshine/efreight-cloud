package com.efreight.afbase.service;

import com.efreight.afbase.entity.CssFinancialAccount;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.FinancialAccount;

import java.util.List;

/**
 * <p>
 * CSS 财务科目 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2021-01-13
 */
public interface CssFinancialAccountService extends IService<CssFinancialAccount> {

    List<FinancialAccount> getList(String businessScope);
}
