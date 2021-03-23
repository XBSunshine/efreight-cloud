package com.efreight.afbase.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.efreight.afbase.entity.FinancialAccount;
import com.efreight.afbase.entity.FinancialAccountTree;
import com.efreight.afbase.entity.TreeNode;
import com.efreight.afbase.entity.WriteOffFinancialAccount;
import com.efreight.afbase.service.FinancialAccountService;
import com.efreight.common.security.service.EUserDetails;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/financialAccount")
@Slf4j
@AllArgsConstructor
public class FinancialAccountController {
    private final FinancialAccountService financialAccountService;

    /**
     * 服务类别列表查询
     *
     * @param businessScope
     * @return
     */
    @GetMapping
    public MessageInfo list(String businessScope) {
        try {
            QueryWrapper<FinancialAccount> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("business_scope", businessScope).eq("org_id", SecurityUtils.getUser().getOrgId()).orderByAsc("financial_account_code");
            return MessageInfo.ok(buildTree(financialAccountService.list(queryWrapper), 0));
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    public List<FinancialAccountTree> buildTree(List<FinancialAccount> menus, int root) {
        List<FinancialAccountTree> trees = new ArrayList<>();
        FinancialAccountTree node;
        for (FinancialAccount menu : menus) {
            node = new FinancialAccountTree();
            node.setId(menu.getFinancialAccountId());
            node.setParentId(menu.getParentId());
            node.setOrgId(menu.getOrgId());
            node.setBusinessScope(menu.getBusinessScope());
            node.setFinancialAccountType(menu.getFinancialAccountType());
            node.setFinancialAccountName(menu.getFinancialAccountName());
            node.setFinancialAccountCode(menu.getFinancialAccountCode());
            node.setManageMode(menu.getManageMode());
            node.setSubsidiaryAccount(menu.getSubsidiaryAccount());
            node.setAccountRemark(menu.getAccountRemark());
            node.setFinancialAccountClass01(menu.getFinancialAccountClass01());
            node.setFinancialAccountClass02(menu.getFinancialAccountClass02());
            node.setFinancialAccountClass03(menu.getFinancialAccountClass03());
            node.setFinancialAccountClass04(menu.getFinancialAccountClass04());
            node.setFinancialAccountClass05(menu.getFinancialAccountClass05());
            node.setIsValid(menu.getIsValid());
            node.setEditorName(!StrUtil.isEmpty(menu.getEditorName()) ? menu.getEditorName() : menu.getCreatorName());
            node.setEditTime(menu.getEditTime() != null ? menu.getEditTime() : menu.getCreateTime());
            //查询父级的科目代码
            FinancialAccount financialAccountParent = financialAccountService.getById(menu.getParentId());
            if(financialAccountParent != null){
                node.setFinancialAccountCodeParent(financialAccountParent.getFinancialAccountCode());
            }
            trees.add(node);
        }
        return buildByLoop(trees, root);
    }

    public <T extends TreeNode> List<T> buildByLoop(List<T> treeNodes, Object root) {

        List<T> trees = new ArrayList<>();
        for (T treeNode : treeNodes) {
            if (root.equals(treeNode.getParentId())) {
                trees.add(treeNode);
            }
            for (T it : treeNodes) {
                if (it.getParentId() == treeNode.getId()) {
                    if (treeNode.getChildren() == null) {
                        treeNode.setChildren(new ArrayList<>());
                    }
                    treeNode.add(it);
                }
            }
        }
        return trees;
    }

    /**
     * 科目代码新建
     *
     * @param financialAccount
     * @return
     */
    @PostMapping
    public MessageInfo save(@RequestBody FinancialAccount financialAccount) {
        try {
            financialAccountService.save(financialAccount);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 根据数据ID删除科目代码
     * @param financialAccountId
     * @return
     */
    @DeleteMapping("delete/{id}")
    public MessageInfo delete(@PathVariable("id") Integer financialAccountId){
        try{
            int result = financialAccountService.delete(financialAccountId);
            return MessageInfo.ok(result);
        }catch (Exception e){
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 科目代码编辑
     *
     * @param financialAccount
     * @return
     */
    @PutMapping
    public MessageInfo edit(@RequestBody FinancialAccount financialAccount) {
        try {
            financialAccountService.edit(financialAccount);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 列出发票核销单-科目
     * @return
     */
    @GetMapping("listWriteOffAccount")
    public MessageInfo listWriteOffAccount(){
        try{
            EUserDetails userDetails = SecurityUtils.getUser();
            List<WriteOffFinancialAccount> financialAccountBeans = financialAccountService.listWriteOffAccount(userDetails.getOrgId());
            return MessageInfo.ok(financialAccountBeans);
        }catch (Exception e){
            log.error(e.getMessage(), e);
            return MessageInfo.failed(e.getMessage());
        }
    }
}
