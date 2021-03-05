package com.efreight.afbase.controller;

import com.efreight.afbase.entity.CssCostInvoice;
import com.efreight.afbase.service.CssCostInvoiceService;
import com.efreight.common.core.annotation.ResponseResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * CSS 应付：发票申请表 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2021-01-04
 */
@RestController
@RequestMapping("/cssCostInvoice")
@ResponseResult
@AllArgsConstructor
@Slf4j
public class CssCostInvoiceController {

    private CssCostInvoiceService cssCostInvoiceService;

    /**
     * 发票提交申请
     *
     * @param cssCostInvoice
     */
    @PostMapping
    public void save(@RequestBody CssCostInvoice cssCostInvoice) {
        cssCostInvoiceService.insert(cssCostInvoice);
    }

    /**
     * 发票申请提交撤销
     *
     * @param paymentId
     */
    @DeleteMapping("/{paymentId}/{rowUuid}")
    public void cancel(@PathVariable("paymentId") Integer paymentId, @PathVariable("rowUuid") String rowUuid) {
        cssCostInvoiceService.cancel(paymentId, rowUuid);
    }

    /**
     * 收票查看详情
     *
     * @param invoiceId
     * @return
     */
    @GetMapping("/{invoiceId}")
    public CssCostInvoice view(@PathVariable("invoiceId") Integer invoiceId) {
        return cssCostInvoiceService.view(invoiceId);
    }

    /**
     * 校验是否收票完毕
     *
     * @param invoiceId
     */
    @GetMapping("/checkIfInvoiceCompleteWhenInsertInvoiceDetail/{invoiceId}/{rowUuid}")
    public void checkIfInvoiceCompleteWhenInsertInvoiceDetail(@PathVariable("invoiceId") Integer invoiceId, @PathVariable("rowUuid") String rowUuid) {
        cssCostInvoiceService.checkIfInvoiceCompleteWhenInsertInvoiceDetail(invoiceId, rowUuid);
    }

    /**
     * 校验账单是否可以付款申请
     *
     * @param paymentId
     */
    @GetMapping("/checkIfCreateInvoice/{paymentId}")
    public void checkIfCreateInvoice(@PathVariable("paymentId") Integer paymentId) {
        cssCostInvoiceService.checkIfCreateInvoice(paymentId);
    }

}

