package com.efreight.afbase.service;

import com.efreight.afbase.entity.CssPReportSupplierProcurement;
import com.efreight.afbase.entity.CssPReportSupplierProcurementDetail;

import java.util.List;

public interface CssPReportSupplierProcurementService {
    List<CssPReportSupplierProcurement> getList(CssPReportSupplierProcurement cssPReportSupplierProcurement);

    List<CssPReportSupplierProcurementDetail> viewDetail(CssPReportSupplierProcurementDetail cssPReportSupplierProcurementDetail);
}
