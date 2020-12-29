package com.efreight.afbase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.FinancialAccount;
import com.efreight.afbase.entity.Service;
import com.efreight.afbase.entity.VPrmCategory;
import com.efreight.afbase.entity.VPrmCategoryTree;

import java.util.List;

public interface FinancialAccountService extends IService<FinancialAccount> {
    int delete(Integer financialAccountId);

    void edit(FinancialAccount financialAccount);
}
