package com.efreight.afbase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.CssVoucherExport;

public interface CssVoucherExportService {
    IPage getPage(Page page, CssVoucherExport cssVoucherExport);

    void exportExcel(CssVoucherExport cssVoucherExport);

    void voucherGenerate(CssVoucherExport cssVoucherExport);

    CssVoucherExport total(CssVoucherExport cssVoucherExport);

    void voucherCallback(CssVoucherExport cssVoucherExport);

    Integer getMaxVoucherNumber(String voucherDate);
}
