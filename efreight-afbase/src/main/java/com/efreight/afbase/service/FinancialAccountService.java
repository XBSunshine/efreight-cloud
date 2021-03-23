package com.efreight.afbase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.entity.WriteOffFinancialAccount;

import java.util.List;

public interface FinancialAccountService extends IService<FinancialAccount> {
    int delete(Integer financialAccountId);

    void edit(FinancialAccount financialAccount);

    /**
     * 发标核销单-科目
     * @return
     */
    List<WriteOffFinancialAccount> listWriteOffAccount(Integer orgId);
}
