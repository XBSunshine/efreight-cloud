package com.efreight.afbase.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.exportExcel.ReportReceivableAgeDetail;
import com.efreight.afbase.entity.procedure.ReportReceivableAge;
import com.efreight.afbase.service.ReportReceivableAgeService;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/reportReceivableAge")
@AllArgsConstructor
@Slf4j
public class ReportReceivableAgeController {

    private ReportReceivableAgeService reportReceivableAgeService;

    /**
     * 查询列表
     *
     * @param page
     * @param reportReceivableAge
     * @return
     */
    @GetMapping
    public MessageInfo getPage(Page page, ReportReceivableAge reportReceivableAge) {
        try {
            Map<String, List> result = reportReceivableAgeService.getPage(page, reportReceivableAge);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查看详情
     * @param reportReceivableAge
     * @return
     */
    @GetMapping("/view")
    public MessageInfo view(ReportReceivableAge reportReceivableAge){
        try {
          List<ReportReceivableAgeDetail> result =  reportReceivableAgeService.view(reportReceivableAge);
          return MessageInfo.ok(result);
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 导出Excel
     *
     * @param
     * @return
     */
    @PostMapping("/exportExcel")
    public void exportExcel(ReportReceivableAge reportReceivableAge) {
        try {
            reportReceivableAgeService.exportExcel(reportReceivableAge);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }
    /**
     * 导出Excel列表
     *
     * @param
     * @return
     */
    @PostMapping("/exportExcelList")
    public void exportExcelList(ReportReceivableAge reportReceivableAge) {
        try {
            reportReceivableAgeService.exportExcelList(reportReceivableAge);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }
}
