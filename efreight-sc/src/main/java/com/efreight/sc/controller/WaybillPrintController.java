package com.efreight.sc.controller;


import com.efreight.common.security.util.MessageInfo;
import com.efreight.sc.entity.WaybillPrint;
import com.efreight.sc.service.WaybillPrintService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * CS 订单管理 海运制单 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-08-17
 */
@RestController
@RequestMapping("/waybillPrint")
@AllArgsConstructor
@Slf4j
public class WaybillPrintController {

    private final WaybillPrintService waybillPrintService;

    /**
     * 通过订单id查询该订单主单号下所有的分单信息
     *
     * @param orderId
     * @return
     */
    @GetMapping("/list/{orderId}")
    public MessageInfo getList(@PathVariable("orderId") Integer orderId) {
        try {
            List<Map<String, String>> list = waybillPrintService.getList(orderId);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 通过orderId   OR  mblNumber 获取制单详情信息
     *
     * @param orderIdOrMblNumber
     * @return
     */
    @GetMapping("/view/{orderIdOrMblNumber}/{flag}")
    public MessageInfo view(@PathVariable("orderIdOrMblNumber") String orderIdOrMblNumber, @PathVariable("flag") String flag) {
        try {
            WaybillPrint waybillPrint = waybillPrintService.view(orderIdOrMblNumber, flag);
            return MessageInfo.ok(waybillPrint);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 新增制单信息
     *
     * @param waybillPrint
     * @return
     */
    @PostMapping
    public MessageInfo save(@RequestBody WaybillPrint waybillPrint) {
        try {
            waybillPrintService.insert(waybillPrint);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 编辑制单信息
     *
     * @param waybillPrint
     * @return
     */
    @PutMapping
    public MessageInfo modify(@RequestBody WaybillPrint waybillPrint) {
        try {
            waybillPrintService.modify(waybillPrint);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 打印 - EXCEL版
     * @param type
     * @param waybillPrintId
     * @return
     */
    @PostMapping("/print/{type}/{waybillPrintId}")
    public void print(@PathVariable("type") String type, @PathVariable("waybillPrintId") Integer waybillPrintId) {
        try {
            waybillPrintService.print(type, waybillPrintId);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}

