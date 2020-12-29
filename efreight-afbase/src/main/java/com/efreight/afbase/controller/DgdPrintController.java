package com.efreight.afbase.controller;


import com.efreight.afbase.entity.DgdPrint;
import com.efreight.afbase.service.DgdPrintService;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * AF 出口订单 DGD 制单 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-01-14
 */
@RestController
@RequestMapping("/dgdPrint")
@AllArgsConstructor
@Slf4j
public class DgdPrintController {

    private final DgdPrintService dgdPrintService;

    /**
     * dgdprint 列表查询
     *
     * @param orderUuid
     * @return
     */
    @GetMapping("/list/{orderUuid}")
    public MessageInfo list(@PathVariable("orderUuid") String orderUuid) {
        try {
            List<DgdPrint> list = dgdPrintService.getList(orderUuid);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 新建
     *
     * @param dgdPrint
     * @return
     */
    @PostMapping
    public MessageInfo save(@RequestBody DgdPrint dgdPrint) {
        try {
            dgdPrintService.saveDgdPrint(dgdPrint);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 修改
     *
     * @param dgdPrint
     * @return
     */
    @PutMapping
    public MessageInfo update(@RequestBody DgdPrint dgdPrint) {
        try {
            dgdPrintService.updateDgdPrint(dgdPrint);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 删除
     *
     * @param dgdPrintId
     * @return
     */
    @DeleteMapping("/{dgdPrintId}")
    public MessageInfo delete(@PathVariable("dgdPrintId") Integer dgdPrintId) {
        try {
            dgdPrintService.delete(dgdPrintId);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查看详情
     *
     * @param dgdPrintId
     * @return
     */
    @GetMapping("/view/{dgdPrintId}")
    public MessageInfo view(@PathVariable("dgdPrintId") Integer dgdPrintId) {
        try {
            DgdPrint dgdPrint = dgdPrintService.view(dgdPrintId);
            return MessageInfo.ok(dgdPrint);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查询当前用户英文名
     *
     * @return
     */
    @GetMapping("/userEnName")
    public MessageInfo getUserInfo() {
        try {
            return MessageInfo.ok(SecurityUtils.getUser().getUserEname());
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 格打
     * @param dgdPrintId
     * @return
     */
    @GetMapping("/printG/{dgdPrintId}")
    public MessageInfo printG(@PathVariable("dgdPrintId") Integer dgdPrintId) {
        try {
            String path = dgdPrintService.printG(dgdPrintId);
            return MessageInfo.ok(path);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 套打
     * @param dgdPrintId
     * @return
     */
    @GetMapping("/printT/{dgdPrintId}")
    public MessageInfo printT(@PathVariable("dgdPrintId") Integer dgdPrintId) {
        try {
            String path = dgdPrintService.printT(dgdPrintId);
            return MessageInfo.ok(path);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}

