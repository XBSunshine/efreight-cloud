package com.efreight.afbase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.CssCostInvoiceDetailWriteoff;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * CSS 应收：发票明细 核销表 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2021-01-04
 */
public interface CssCostInvoiceDetailWriteoffService extends IService<CssCostInvoiceDetailWriteoff> {

    IPage getPage(Page page, CssCostInvoiceDetailWriteoff cssCostInvoiceDetailWriteoff);

    void insert(CssCostInvoiceDetailWriteoff cssCostInvoiceDetailWriteoff);

    void delete(Integer invoiceDetailWriteoffId, String rowUuid);

    void checkIfCompleteVoucher(Integer invoiceDetailWriteoffId);

    void exportExcel(CssCostInvoiceDetailWriteoff cssCostInvoiceDetailWriteoff);

    String getWriteoffNum(String businessScope);
}
