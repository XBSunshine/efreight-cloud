package com.efreight.afbase.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.Airport;
import com.efreight.afbase.entity.Warehouse;
import com.efreight.afbase.service.AirportService;
import com.efreight.afbase.service.WarehouseService;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@RestController
@AllArgsConstructor
@RequestMapping("/warehouse")
@Slf4j
public class WarehouseController {

    private final WarehouseService service;
    private final AirportService airportService;

    /**
     * 分页查询信息
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/page")
    public MessageInfo getListPage(Page page, Warehouse bean) {
        return MessageInfo.ok(service.getListPage(page, bean));
    }

    /**
     * 添加
     *
     * @param bean 实体
     * @return success/false
     */
    @PostMapping(value = "/doSave")
    @PreAuthorize("@pms.hasPermission('sys_base_warehouse_add')")
    public MessageInfo doSave(@Valid @RequestBody Warehouse bean) {
        List<Airport> list = airportService.isHaved(bean.getApCode(), "CN");
        if (list.size() == 0) {
            return MessageInfo.failed("机场代码：" + bean.getApCode() + "不存在");
        }
        return MessageInfo.ok(service.doSave(bean));
    }

    /**
     * 修改
     *
     * @param bean 实体
     * @return success/false
     */
    @PostMapping(value = "/doUpdate")
    @PreAuthorize("@pms.hasPermission('sys_base_warehouse_edit')")
    public MessageInfo doUpdate(@Valid @RequestBody Warehouse bean) {
        return MessageInfo.ok(service.doUpdate(bean));
    }

    /**
     * 通过始发港查找货栈和库房
     *
     * @param departureStation
     * @param type
     * @return
     */
    @GetMapping("/getByDeparture")
    public MessageInfo getListByDeparture(String departureStation, String type) {
        try {
            return MessageInfo.ok(service.getListByDeparture(departureStation, type));
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查询有效库房或货栈
     * @param type
     * @return
     */
    @GetMapping("/{type}")
    public MessageInfo list(@PathVariable("type") String type){
        try {
            List<Warehouse> list = service.getList(type);
            return MessageInfo.ok(list);
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 通过机场代码查询交货通知模板
     *
     * @param apCode
     * @return
     */
    @GetMapping("/findshipperTemplates")
    public MessageInfo findshipperTemplates(String apCode) {
        try {
            return MessageInfo.ok(service.findshipperTemplates(apCode));
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 根据数据ID删除数据
     * @param warehouseId
     * @return
     */
    @DeleteMapping("/{warehouseId}")
    @PreAuthorize("@pms.hasPermission('sys_base_warehouse_delete')")
    public MessageInfo delete(@PathVariable("warehouseId") Integer warehouseId){
        try{
            int result = service.deleteById(warehouseId);
            return MessageInfo.ok(result);
        }catch (Exception e){
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }
    /**
     * @param businessScope
     * @return
     */
    @GetMapping("/getWarehouseListByQuery/{businessScope}")
    public MessageInfo getWarehouseListByQuery(@PathVariable("businessScope") String businessScope) {
        try {
            return MessageInfo.ok(service.getWarehouseListByQuery(businessScope));
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}

