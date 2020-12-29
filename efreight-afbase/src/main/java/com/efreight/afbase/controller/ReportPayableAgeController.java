package com.efreight.afbase.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.exportExcel.ReportPayableAgeDetail;
import com.efreight.afbase.entity.procedure.ReportPayableAge;
import com.efreight.afbase.service.ReportPayableAgeService;
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
@RequestMapping("/reportPayableAge")
@AllArgsConstructor
@Slf4j
public class ReportPayableAgeController {

    private ReportPayableAgeService reportPayableAgeService;

    /**
     * 查询列表-AF
     *
     * @param page
     * @param reportPayableAge
     * @return
     */
    @GetMapping("/af")
    public MessageInfo getPageForAF(Page page, ReportPayableAge reportPayableAge) {
        try {
            Map<String, List> result = reportPayableAgeService.getPageForAF(page, reportPayableAge);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查看详情-AF
     *
     * @param reportPayableAge
     * @return
     */
    @GetMapping("/af/view")
    public MessageInfo viewForAF(ReportPayableAge reportPayableAge) {
        try {
            List<ReportPayableAgeDetail> result = reportPayableAgeService.viewForAF(reportPayableAge);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }


    /**
     * 导出Excel-AF
     *
     * @param
     * @return
     */
    @PostMapping("/af/exportExcel")
    public void exportExcelForAF(ReportPayableAge reportPayableAge) {
        try {
            reportPayableAgeService.exportExcelForAF(reportPayableAge);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    /**
     * 导出ExcelList-AF
     *
     * @param
     * @return
     */
    @PostMapping("/af/exportExcelList")
    public void exportExcelListForAF(ReportPayableAge reportPayableAge) {
        try {
            reportPayableAgeService.exportExcelListForAF(reportPayableAge);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    /**
     * 查询列表-SC
     *
     * @param page
     * @param reportPayableAge
     * @return
     */
    @GetMapping("/sc")
    public MessageInfo getPageForSC(Page page, ReportPayableAge reportPayableAge) {
        try {
            Map<String, List> result = reportPayableAgeService.getPageForSC(page, reportPayableAge);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查看详情-SC
     *
     * @param reportPayableAge
     * @return
     */
    @GetMapping("/sc/view")
    public MessageInfo viewForSC(ReportPayableAge reportPayableAge) {
        try {
            List<ReportPayableAgeDetail> result = reportPayableAgeService.viewForSC(reportPayableAge);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }


    /**
     * 导出Excel-SC
     *
     * @param
     * @return
     */
    @PostMapping("/sc/exportExcel")
    public void exportExcelForSC(ReportPayableAge reportPayableAge) {
        try {
            reportPayableAgeService.exportExcelForSC(reportPayableAge);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    /**
     * 导出ExcelList-SC
     *
     * @param
     * @return
     */
    @PostMapping("/sc/exportExcelList")
    public void exportExcelListForSC(ReportPayableAge reportPayableAge) {
        try {
            reportPayableAgeService.exportExcelListForSC(reportPayableAge);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }
    
    /**
     * 查询列表-TC\LC\IO
     *
     * @param page
     * @param reportPayableAge
     * @return
     */
    @GetMapping("/page")
    public MessageInfo getPageList(Page page, ReportPayableAge reportPayableAge) {
        try {
            Map<String, List> result = reportPayableAgeService.getPageList(page, reportPayableAge);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
    /**
     * 查看详情-TC/LC/IO
     *
     * @param reportPayableAge
     * @return
     */
    @GetMapping("/page/view")
    public MessageInfo view(ReportPayableAge reportPayableAge) {
        try {
            List<ReportPayableAgeDetail> result = reportPayableAgeService.view(reportPayableAge);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }


    /**
     * 导出Excel-TC/LC/IO 详情
     *
     * @param
     * @return
     */
    @PostMapping("/page/exportExcel")
    public void exportExcel(ReportPayableAge reportPayableAge) {
        try {
            reportPayableAgeService.exportExcel(reportPayableAge);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    /**
     * 导出ExcelList-TC/LC/IO 列表
     *
     * @param
     * @return
     */
    @PostMapping("/page/exportExcelList")
    public void exportExcelList(ReportPayableAge reportPayableAge) {
        try {
            reportPayableAgeService.exportExcelList(reportPayableAge);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }
}
