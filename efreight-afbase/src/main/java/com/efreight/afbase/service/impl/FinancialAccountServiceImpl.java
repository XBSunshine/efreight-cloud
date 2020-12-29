package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.FinancialAccountMapper;
import com.efreight.afbase.dao.ServiceMapper;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.service.FinancialAccountService;
import com.efreight.afbase.service.LogService;
import com.efreight.afbase.service.ServiceService;
import com.efreight.afbase.service.VPrmCategoryService;
import com.efreight.afbase.utils.FormatUtils;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@AllArgsConstructor
@Slf4j
public class FinancialAccountServiceImpl extends ServiceImpl<FinancialAccountMapper, FinancialAccount> implements FinancialAccountService {
    private final LogService logService;

    @Override
    public boolean save(FinancialAccount financialAccount) {

        //查询所有的父级，当达到五级则不允许继续新增子级
        FinancialAccountLevel financialAccountLevel = baseMapper.getFinancialAccountAllParent(financialAccount.getIdParent());
        if(financialAccountLevel !=null && financialAccountLevel.getFinancialAccountIdE() != null){
            throw new RuntimeException("科目代码不能超过五级");
        }else{
            boolean save = false;
            FinancialAccount financialAccountParent = baseMapper.selectById(financialAccount.getIdParent());
            if(financialAccountParent != null){
                financialAccount.setBusinessScope(financialAccountParent.getBusinessScope());
                financialAccount.setParentId(financialAccountParent.getFinancialAccountId());
                financialAccount.setFinancialAccountType(financialAccountParent.getFinancialAccountType());
                financialAccount.setFinancialAccountCode(financialAccountParent.getFinancialAccountCode() + financialAccount.getFinancialAccountCode());
                financialAccount.setFinancialAccountClass01(financialAccountParent.getFinancialAccountClass01());
                financialAccount.setFinancialAccountClass02(financialAccountParent.getFinancialAccountClass02());
                financialAccount.setFinancialAccountClass03(financialAccountParent.getFinancialAccountClass03());
                financialAccount.setFinancialAccountClass04(financialAccountParent.getFinancialAccountClass04());
                financialAccount.setFinancialAccountClass05(financialAccountParent.getFinancialAccountClass05());
                financialAccount.setCreateTime(LocalDateTime.now());
                financialAccount.setCreatorId(SecurityUtils.getUser().getId());
                financialAccount.setCreatorName(SecurityUtils.getUser().getUserCname());
                financialAccount.setOrgId(SecurityUtils.getUser().getOrgId());
                save = super.save(financialAccount);
            }
            return save;
        }
    }

    @Override
    public int delete(Integer financialAccountId) {
        Integer data = 0;
        if(null != financialAccountId){
            data = this.baseMapper.deleteById(financialAccountId);
        }
        return data;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void edit(FinancialAccount financialAccount) {
        FinancialAccount financialAccountParent = baseMapper.selectById(financialAccount.getParentId());
        String financialAccountCodeOld = financialAccount.getFinancialAccountCodeOld();
        String financialAccountCode = financialAccount.getFinancialAccountCode();
        if(financialAccountParent != null){
            financialAccountCode = financialAccountParent.getFinancialAccountCode() + financialAccount.getFinancialAccountCode();
            financialAccountCodeOld = financialAccountParent.getFinancialAccountCode() + financialAccount.getFinancialAccountCodeOld();
            financialAccount.setFinancialAccountCode(financialAccountCode);
        }
        financialAccount.setEditorId(SecurityUtils.getUser().getId());
        financialAccount.setEditorName(SecurityUtils.getUser().getUserCname());
        financialAccount.setEditTime(LocalDateTime.now());
        updateById(financialAccount);
        //更新子级科目代码
       List<FinancialAccountLevel> list = baseMapper.getFinancialAccountLevel(financialAccount.getFinancialAccountId());
       if(list != null && list.size() > 0){
           for (int i = 0; i < list.size(); i++) {
               FinancialAccount fa = new FinancialAccount();
               FinancialAccountLevel financialAccountLevel = list.get(i);
               if(financialAccountLevel != null){
                   if(!StrUtil.isEmpty(financialAccountLevel.getFinancialAccountCodeB())){
                       fa.setFinancialAccountId(financialAccountLevel.getFinancialAccountIdB());
                       fa.setFinancialAccountCode(financialAccountCode + financialAccountLevel.getFinancialAccountCodeB().substring(financialAccountCodeOld.length()));
                       updateById(fa);
                   }
                   if(!StrUtil.isEmpty(financialAccountLevel.getFinancialAccountCodeC())){
                       fa.setFinancialAccountId(financialAccountLevel.getFinancialAccountIdC());
                       fa.setFinancialAccountCode(financialAccountCode + financialAccountLevel.getFinancialAccountCodeC().substring(financialAccountCodeOld.length()));
                       updateById(fa);
                   }
                   if(!StrUtil.isEmpty(financialAccountLevel.getFinancialAccountCodeD())){
                       fa.setFinancialAccountId(financialAccountLevel.getFinancialAccountIdD());
                       fa.setFinancialAccountCode(financialAccountCode + financialAccountLevel.getFinancialAccountCodeD().substring(financialAccountCodeOld.length()));
                       updateById(fa);
                   }
                   if(!StrUtil.isEmpty(financialAccountLevel.getFinancialAccountCodeE())){
                       fa.setFinancialAccountId(financialAccountLevel.getFinancialAccountIdE());
                       fa.setFinancialAccountCode(financialAccountCode + financialAccountLevel.getFinancialAccountCodeE().substring(financialAccountCodeOld.length()));
                       updateById(fa);
                   }
               }
           }
       }
    }

}
