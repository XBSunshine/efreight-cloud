package com.efreight.afbase.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.CssCostInvoiceDetail;
import com.efreight.afbase.service.CssCostInvoiceDetailService;
import com.efreight.common.core.annotation.ResponseResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * CSS 应付：发票明细表 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2021-01-04
 */
@RestController
@RequestMapping("/cssCostInvoiceDetail")
@ResponseResult
@AllArgsConstructor
@Slf4j
public class CssCostInvoiceDetailController {

    private final CssCostInvoiceDetailService cssCostInvoiceDetailService;

    /**
     * 发票列表查询
     *
     * @param cssCostInvoiceDetail
     * @return
     */
    @GetMapping
    public IPage<CssCostInvoiceDetail> page(Page page, CssCostInvoiceDetail cssCostInvoiceDetail) {
        return cssCostInvoiceDetailService.getPage(page, cssCostInvoiceDetail);
    }

    /**
     * 新增收票
     *
     * @param cssCostInvoiceDetail
     */
    @PostMapping
    public void save(@RequestBody CssCostInvoiceDetail cssCostInvoiceDetail) {
        cssCostInvoiceDetailService.insert(cssCostInvoiceDetail);
    }

    /**
     * 删除发票
     *
     * @param invoiceDetailId
     * @param rowUuid
     */
    @DeleteMapping("/{invoiceDetailId}/{rowUuid}")
    public void delete(@PathVariable("invoiceDetailId") Integer invoiceDetailId, @PathVariable("rowUuid") String rowUuid) {
        cssCostInvoiceDetailService.delete(invoiceDetailId, rowUuid);
    }

    /**
     * 校验发票是否满足核销条件
     *
     * @param invoiceDetailId
     */
    @GetMapping("/checkIfWriteoffComplete/{invoiceDetailId}")
    public void checkIfWriteoffComplete(@PathVariable("invoiceDetailId") Integer invoiceDetailId) {
        cssCostInvoiceDetailService.checkIfWriteoffComplete(invoiceDetailId);
    }

    /**
     * 查询发票详情
     *
     * @param invoiceDetailId
     * @return
     */
    @GetMapping("/view/{invoiceDetailId}")
    public CssCostInvoiceDetail view(@PathVariable("invoiceDetailId") Integer invoiceDetailId) {
        return cssCostInvoiceDetailService.view(invoiceDetailId);
    }

    /**
     * 列表导出
     * @param cssCostInvoiceDetail
     */
    @PostMapping("/exportExcel")
    public void exportExcel(CssCostInvoiceDetail cssCostInvoiceDetail) {
        cssCostInvoiceDetailService.exportExcel(cssCostInvoiceDetail);
    }
}

