package com.efreight.afbase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.CssCostWriteoff;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.CssCostWriteoffDetail;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * CSS 应付：核销 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-02-14
 */
public interface CssCostWriteoffService extends IService<CssCostWriteoff> {

    List<CssCostWriteoffDetail> automatch(Integer paymentId, BigDecimal amountWriteoff);

    void insert(CssCostWriteoff cssCostWriteoff);

    void delete(Integer costWriteoffId);

    CssCostWriteoff view(Integer costWriteoffId);

    IPage getPage(Page page, CssCostWriteoff cssCostWriteoff);

    List<CssCostWriteoff> exportWriteoffExcel(CssCostWriteoff cssCostWriteoff);

    CssCostWriteoff getVoucherDate(Integer costWriteoffId);
}
