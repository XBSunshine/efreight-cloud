package com.efreight.afbase.controller;


import com.efreight.afbase.entity.CssIncomeWriteoffDetail;
import com.efreight.afbase.service.CssIncomeWriteoffDetailService;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * CSS 应收：核销 明细 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-02-03
 */
@RestController
@RequestMapping("/cssIncomeWriteoffDetail")
@AllArgsConstructor
@Slf4j
public class CssIncomeWriteoffDetailController {

    private final CssIncomeWriteoffDetailService cssIncomeWriteoffDetailService;

    /**
     * 查看账单核销明细页面列表详情
     * @param incomeWriteoffId
     * @return
     */
    @GetMapping("/{incomeWriteoffId}")
    public MessageInfo queryDebitNoteWriteoffDetailList(@PathVariable("incomeWriteoffId") Integer incomeWriteoffId) {
        try {
            CssIncomeWriteoffDetail cssIncomeWriteoffDetail = cssIncomeWriteoffDetailService.queryDebitNoteWriteoffDetailList(incomeWriteoffId);
            return MessageInfo.ok(cssIncomeWriteoffDetail);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

}

