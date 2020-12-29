package com.efreight.afbase.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.CssVoucherExport;
import com.efreight.afbase.service.CssVoucherExportService;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * create 2020/10/12
 * since xiaobo
 * css 凭证导出
 */
@RestController
@RequestMapping("/cssVoucherExport")
@AllArgsConstructor
@Slf4j
public class CssVoucherExportController {

    private final CssVoucherExportService cssVoucherExportService;

    /**
     * 列表分页查询
     *
     * @param page
     * @param cssVoucherExport
     * @return
     */
    @GetMapping
    public MessageInfo page(Page page, CssVoucherExport cssVoucherExport) {
        try {
            IPage result = cssVoucherExportService.getPage(page, cssVoucherExport);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 合计
     *
     * @param cssVoucherExport
     * @return
     */
    @GetMapping("/total")
    public MessageInfo total(CssVoucherExport cssVoucherExport) {
        try {
            CssVoucherExport total = cssVoucherExportService.total(cssVoucherExport);
            return MessageInfo.ok(total);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 列表导出
     *
     * @param cssVoucherExport
     */
    @PostMapping("/exportExcel")
    public void exportExcel(CssVoucherExport cssVoucherExport) {
        try {
            cssVoucherExportService.exportExcel(cssVoucherExport);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 生成凭证
     *
     * @param cssVoucherExport
     * @return
     */
    @PostMapping("/voucherGenerate")
    public void voucherGenerate(@RequestBody CssVoucherExport cssVoucherExport) {
        try {
            cssVoucherExportService.voucherGenerate(cssVoucherExport);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 凭证退回
     *
     * @param cssVoucherExport
     * @return
     */
    @PostMapping("/voucherCallback")
    public MessageInfo voucherCallback(@RequestBody CssVoucherExport cssVoucherExport) {
        try {
            cssVoucherExportService.voucherCallback(cssVoucherExport);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 获取当月最大凭证号
     *
     * @param voucherDate
     * @return
     */
    @GetMapping("/getMaxVoucherNumber/{voucherDate}")
    public MessageInfo getMaxVoucherNumber(@PathVariable("voucherDate") String voucherDate) {
        try {
            Integer maxVoucherNumber = cssVoucherExportService.getMaxVoucherNumber(voucherDate);
            return MessageInfo.ok(maxVoucherNumber);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}
