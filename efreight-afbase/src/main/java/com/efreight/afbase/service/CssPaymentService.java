package com.efreight.afbase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.AfCost;
import com.efreight.afbase.entity.CssCostWriteoffDetail;
import com.efreight.afbase.entity.CssPayment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.PaymentBatchDetail;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * CSS 成本对账单 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-02-05
 */
public interface CssPaymentService extends IService<CssPayment> {

    IPage getPage(Page page, CssPayment cssPayment);

    void insert(CssPayment cssPayment);

    void insertIO(CssPayment cssPayment);

    void insertSc(CssPayment cssPayment);
    void insertTc(CssPayment cssPayment);
    void insertLc(CssPayment cssPayment);

    void modify(CssPayment cssPayment);

    CssPayment view(Integer paymentId);

    void exportExcel(Integer paymentId);

    void delete(Integer paymentId, String rowUuid);

    List<AfCost> getAutomatchCostList(AfCost afCost);

    List<CssCostWriteoffDetail> getPaymentDetailByPaymentId(Integer paymentId);

    List<CssPayment> exportPaymentExcel(CssPayment cssPayment);

    void invoiceRemark(CssPayment bean);

    List<PaymentBatchDetail> readExcel(String businessScope, Integer customerId, String currency, String serviceIds, MultipartFile file);

    void exportBatchDetail(PaymentBatchDetail paymentBatchDetail);

    void downloadModel();

    void savePaymentBatch(PaymentBatchDetail paymentBatchDetail);
}
