package com.efreight.afbase.service;

import com.efreight.afbase.entity.CssCostInvoice;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * CSS 应付：发票申请表 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2021-01-04
 */
public interface CssCostInvoiceService extends IService<CssCostInvoice> {

    void insert(CssCostInvoice cssCostInvoice);

    void cancel(Integer paymentId, String rowUuid);

    CssCostInvoice view(Integer invoiceId);

    void checkIfInvoiceCompleteWhenInsertInvoiceDetail(Integer invoiceId, String rowUuid);

    void checkIfCreateInvoice(Integer paymentId);
}
