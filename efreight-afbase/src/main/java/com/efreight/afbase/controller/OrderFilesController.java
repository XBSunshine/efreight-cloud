package com.efreight.afbase.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.OrderFiles;
import com.efreight.afbase.service.OrderFilesService;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * AF 订单管理 出口订单附件 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-02-12
 */
@RestController
@RequestMapping("/orderFiles")
@AllArgsConstructor
@Slf4j
public class OrderFilesController {

    private final OrderFilesService orderFilesService;

    /**
     * 分页列表查询
     *
     * @param page
     * @param orderFiles
     * @return
     */
    @GetMapping
    public MessageInfo getPage(Page page, OrderFiles orderFiles) {
        try {
            IPage result = orderFilesService.getPage(page, orderFiles);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 新建
     *
     * @param orderFiles
     * @return
     */
    @PostMapping("/doSave")
    public MessageInfo save(@RequestBody OrderFiles orderFiles) {
        try {
            orderFilesService.insert(orderFiles);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 批量新建
     *
     * @param orderFiles
     * @return
     */
    @PostMapping("/doBatchSave")
    public MessageInfo doBatchSave(@RequestBody OrderFiles orderFiles) {
        try {
            orderFilesService.insertBatch(orderFiles);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 批量新建 FOR AF
     *
     * @param orderFilesList
     * @return
     */
    @PostMapping("/doBatchSaveForAF")
    public MessageInfo doBatchSaveForAF(@RequestBody List<OrderFiles> orderFilesList) {
        try {
            orderFilesService.insertBatchForAF(orderFilesList);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 删除
     *
     * @param bean
     * @return
     */
    @PostMapping("/doDelete")
    public MessageInfo delete(@RequestBody OrderFiles bean) {
        try {
            orderFilesService.delete(bean);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 更新显示不显示
     *
     * @param
     * @return
     */
    @PostMapping("/showFile")
    public MessageInfo showFile(@RequestBody OrderFiles bean) {
        try {
            orderFilesService.showFile(bean);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 根据订单查询附件列表
     * @param bean
     * @return
     */
    @PostMapping("/getList")
    public MessageInfo list(@RequestBody OrderFiles bean){
        try {
            List<OrderFiles> list = orderFilesService.getList(bean.getOrderId(),bean.getBusinessScope());
            return MessageInfo.ok(list);
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
    /**
     * 协作订单 -- 目前只支持AE 根据订单查询附件列表 过滤不对外的数据
     * @param bean
     * @return
     */
    @PostMapping("/getListByWhere")
    public MessageInfo getListByWhere(@RequestBody OrderFiles bean){
        try {
            List<OrderFiles> list = orderFilesService.getListByWhere(bean);
            return MessageInfo.ok(list);
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

}

