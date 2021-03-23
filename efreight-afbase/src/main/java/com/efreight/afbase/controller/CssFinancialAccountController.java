package com.efreight.afbase.controller;


import com.efreight.afbase.entity.FinancialAccount;
import com.efreight.afbase.service.CssFinancialAccountService;
import com.efreight.common.core.annotation.ResponseResult;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * CSS 财务科目 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2021-01-13
 */
@RestController
@RequestMapping("/cssFinancialAccount")
@AllArgsConstructor
@ResponseResult
public class CssFinancialAccountController {

    private final CssFinancialAccountService cssFinancialAccountService;

    /**
     * 获取科目列表
     * @param businessScope
     * @return
     */
    @GetMapping("/{businessScope}")
    public List<FinancialAccount> lis(@PathVariable("businessScope") String businessScope){
        return cssFinancialAccountService.getList(businessScope);
    }
}

