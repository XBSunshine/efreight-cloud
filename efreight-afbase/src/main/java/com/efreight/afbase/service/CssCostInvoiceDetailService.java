package com.efreight.afbase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.CssCostInvoiceDetail;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * CSS 应付：发票明细表 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2021-01-04
 */
public interface CssCostInvoiceDetailService extends IService<CssCostInvoiceDetail> {

    IPage<CssCostInvoiceDetail> getPage(Page page, CssCostInvoiceDetail cssCostInvoiceDetail);

    void delete(Integer invoiceDetailId, String rowUuid);

    void insert(CssCostInvoiceDetail cssCostInvoiceDetail);

    void checkIfWriteoffComplete(Integer invoiceDetailId);

    CssCostInvoiceDetail view(Integer invoiceDetailId);

    void exportExcel(CssCostInvoiceDetail cssCostInvoiceDetail);
}
