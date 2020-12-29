package com.efreight.sc.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.sc.entity.PortMaintenance;
import com.efreight.sc.service.PortMaintenanceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * CS 海运港口表 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-03-03
 */
@RestController
@RequestMapping("/portMaintenance")
@Slf4j
@AllArgsConstructor
public class PortMaintenanceController {

    private final PortMaintenanceService portMaintenanceService;

    /**
     * 获取所有港口信息
     *
     * @return
     */
    @GetMapping
    public MessageInfo getList() {
        try {
            List<PortMaintenance> list = portMaintenanceService.getList();
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 港口查询分页
     * @param page
     * @param portMaintenance
     * @return
     */
    @GetMapping(value="/page")
    public MessageInfo getPage(Page page, PortMaintenance portMaintenance) {
        try {
            IPage result = portMaintenanceService.getPage(page, portMaintenance);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 保存信息
     * @param portMaintenance
     * @return
     */
    @PostMapping("save")
    public MessageInfo save(@RequestBody PortMaintenance portMaintenance){
        try {
            int result = portMaintenanceService.savePortMaintenance(portMaintenance);
            return MessageInfo.ok(result);
        }catch (Exception e){
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @PostMapping("edit")
    public MessageInfo edit(@RequestBody PortMaintenance portMaintenance){
        try {
            int result = portMaintenanceService.editById(portMaintenance);
            return MessageInfo.ok(result);
        }catch (Exception e){
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    @GetMapping("search/{key}")
    public MessageInfo search(@PathVariable("key") String key){
        List<PortMaintenance> searchResults = portMaintenanceService.search(key);
        return MessageInfo.ok(searchResults);
    }
}

