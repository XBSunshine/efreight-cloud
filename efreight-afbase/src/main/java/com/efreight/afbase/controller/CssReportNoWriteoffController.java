package com.efreight.afbase.controller;


import com.efreight.afbase.entity.CssReportNoWriteoff;
import com.efreight.afbase.entity.CssReportNoWriteoffDetail;
import com.efreight.afbase.service.CssReportNoWriteoffService;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cssReportNoWriteoff")
@Slf4j
@AllArgsConstructor
public class CssReportNoWriteoffController {

    private final CssReportNoWriteoffService cssReportNoWriteoffService;

    /**
     * 获取统计客户未核销金额列表
     * @param cssReportNoWriteoff
     * @return
     */
    @GetMapping
    public MessageInfo getList(CssReportNoWriteoff cssReportNoWriteoff) {
        try {
            List<CssReportNoWriteoff> list = cssReportNoWriteoffService.getList(cssReportNoWriteoff);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 获取统计客户未核销金额详情
     * @param coopId
     * @return
     */
    @GetMapping(value = {"/{coopId}/{type}", "/{coopId}/{type}/{otherOrg}"})
    public MessageInfo getList(@PathVariable("coopId") Integer coopId,
    		                   @PathVariable("type") Integer type,
    		                   @PathVariable(value = "otherOrg", required = false) Integer otherOrg) {
        try {
           List<CssReportNoWriteoffDetail> list = cssReportNoWriteoffService.view(coopId,type,otherOrg);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 详情导出
     * @param coopId
     */
    @PostMapping(value = {"/exportExcel/{coopId}/{type}", "/exportExcel/{coopId}/{type}/{otherOrg}"})
    public void exportExcel(@PathVariable("coopId") Integer coopId,
    		                @PathVariable("type") Integer type,
    		                @PathVariable(value = "otherOrg", required = false) Integer otherOrg){
        try {
            cssReportNoWriteoffService.exportExcel(coopId,type,otherOrg);
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }

    /**
     * 列表导出
     *
     * @param
     * @return
     */
    @PostMapping("/exportExcelList")
    public void exportExcelList(CssReportNoWriteoff cssReportNoWriteoff) {
        try {
            cssReportNoWriteoffService.exportExcelList(cssReportNoWriteoff);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }
}
