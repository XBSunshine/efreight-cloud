package com.efreight.afbase.controller;

import com.efreight.afbase.entity.CssReportIncomeStatement;
import com.efreight.afbase.service.CssReportIncomeStatementService;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 损益表
 * author xiaobo
 * since 2020-10-19
 */
@RestController
@RequestMapping("/cssReportIncomeStatement")
@AllArgsConstructor
@Slf4j
public class CssReportIncomeStatementController {

    private final CssReportIncomeStatementService cssReportIncomeStatementService;

    /**
     * 列表查询
     * @param cssReportIncomeStatement
     * @return
     */
    @GetMapping
    public MessageInfo list(CssReportIncomeStatement cssReportIncomeStatement) {
        try {
            List<Map<String, Object>> list = cssReportIncomeStatementService.list(cssReportIncomeStatement);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 导出
     * @param cssReportIncomeStatement
     */
    @PostMapping("/exportExcel")
    public void export(CssReportIncomeStatement cssReportIncomeStatement){
        try {
            cssReportIncomeStatementService.export(cssReportIncomeStatement);
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }
}
