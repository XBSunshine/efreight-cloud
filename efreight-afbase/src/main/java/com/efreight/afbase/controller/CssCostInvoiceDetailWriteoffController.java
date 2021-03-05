package com.efreight.afbase.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.CssCostInvoiceDetailWriteoff;
import com.efreight.afbase.service.CssCostInvoiceDetailWriteoffService;
import com.efreight.common.core.annotation.ResponseResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * CSS 应收：发票明细 核销表 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2021-01-04
 */
@RestController
@RequestMapping("/cssCostInvoiceDetailWriteoff")
@ResponseResult
@Slf4j
@AllArgsConstructor
public class CssCostInvoiceDetailWriteoffController {

    private final CssCostInvoiceDetailWriteoffService cssCostInvoiceDetailWriteoffService;

    /**
     * 核销列表查询
     *
     * @param page
     * @param cssCostInvoiceDetailWriteoff
     * @return
     */
    @GetMapping
    public IPage getPage(Page page, CssCostInvoiceDetailWriteoff cssCostInvoiceDetailWriteoff) {
        return cssCostInvoiceDetailWriteoffService.getPage(page, cssCostInvoiceDetailWriteoff);
    }

    /**
     * 发票核销
     *
     * @param cssCostInvoiceDetailWriteoff
     */
    @PostMapping
    public void save(@RequestBody CssCostInvoiceDetailWriteoff cssCostInvoiceDetailWriteoff) {
        cssCostInvoiceDetailWriteoffService.insert(cssCostInvoiceDetailWriteoff);
    }

    /**
     * 删除核销单
     *
     * @param invoiceDetailWriteoffId
     */
    @DeleteMapping("/{invoiceDetailWriteoffId}/{rowUuid}")
    public void delete(@PathVariable("invoiceDetailWriteoffId") Integer invoiceDetailWriteoffId,@PathVariable("rowUuid") String rowUuid) {
        cssCostInvoiceDetailWriteoffService.delete(invoiceDetailWriteoffId,rowUuid);
    }

    /**
     * 校验是否已做凭证
     * @param invoiceDetailWriteoffId
     */
    @GetMapping("/checkIfCompleteVoucher/{invoiceDetailWriteoffId}")
    public void checkIfCompleteVoucher(@PathVariable("invoiceDetailWriteoffId") Integer invoiceDetailWriteoffId){
        cssCostInvoiceDetailWriteoffService.checkIfCompleteVoucher(invoiceDetailWriteoffId);
    }

    /**
     * 列表导出
     * @param cssCostInvoiceDetailWriteoff
     */
    @PostMapping("/exportExcel")
    public void exportExcel(CssCostInvoiceDetailWriteoff cssCostInvoiceDetailWriteoff) {
        cssCostInvoiceDetailWriteoffService.exportExcel(cssCostInvoiceDetailWriteoff);
    }
}

