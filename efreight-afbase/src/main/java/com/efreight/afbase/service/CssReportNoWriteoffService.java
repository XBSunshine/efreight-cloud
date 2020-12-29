package com.efreight.afbase.service;

import com.efreight.afbase.entity.CssReportNoWriteoff;
import com.efreight.afbase.entity.CssReportNoWriteoffDetail;

import java.util.List;

public interface CssReportNoWriteoffService {
    List<CssReportNoWriteoff> getList(CssReportNoWriteoff cssReportNoWriteoff);

    List<CssReportNoWriteoffDetail> view(Integer coopId,Integer type,Integer otherOrg);

    void exportExcel(Integer coopId, Integer type,Integer otherOrg);

    void exportExcelList(CssReportNoWriteoff cssReportNoWriteoff);
}
