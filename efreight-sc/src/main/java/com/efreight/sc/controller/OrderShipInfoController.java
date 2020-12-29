package com.efreight.sc.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.sc.entity.OrderShipInfo;
import com.efreight.sc.service.OrderShipInfoService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

import javax.validation.Valid;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-03-03
 */
@RestController
@RequestMapping("/orderShipInfo")
@Slf4j
@AllArgsConstructor
public class OrderShipInfoController {

    private final OrderShipInfoService orderShipInfoService;

    /**
     * 查询签约公司所有的船名
     * @return
     */
    @GetMapping
    public MessageInfo list() {
        try {
            List<OrderShipInfo> list = orderShipInfoService.getList();
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
    /**
     * 分页查询船名
     * @param page
     * @param info
     * @return
     */
    @GetMapping("/pageList")
    public MessageInfo getPageList(Page page,OrderShipInfo info) {
    	try {
    		IPage result = orderShipInfoService.getPageList(page,info);
    	    return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
    
    @PutMapping("/delete/{shipInfoId}")
    public MessageInfo deleteInfo(@PathVariable("shipInfoId") Integer shipInfoId){
    	 try {
    		 orderShipInfoService.deleteInfoById(shipInfoId);
             return MessageInfo.ok();
         }catch (Exception e){
             log.info(e.getMessage());
             return MessageInfo.failed(e.getMessage());
         }
    }
    
    @GetMapping("/view/{shipInfoId}")
    public MessageInfo view(@PathVariable("shipInfoId") Integer shipInfoId) {
        try {
        	OrderShipInfo info = orderShipInfoService.queryOne(shipInfoId);
            return MessageInfo.ok(info);
        } catch (Exception e) {
            return MessageInfo.failed(e.getMessage());
        }
    }
    
    /**
     * 添加
     *
     * @param bean 实体
     * @return success/false
     */
    @PostMapping(value = "/doSave")
    //@PreAuthorize("@pms.hasPermission('sys_base_carrier_add')")
    public MessageInfo addInfo(@Valid @RequestBody OrderShipInfo bean) throws ParseException {
    	
        return MessageInfo.ok(orderShipInfoService.addInfo(bean));
    }
    
    /**
     * 修改
     *
     * @param bean 实体
     * @return success/false
     */
    @PostMapping(value = "/doUpdate")
    //@PreAuthorize("@pms.hasPermission('sys_base_carrier_edit')")
    public MessageInfo doUpdate(@Valid @RequestBody OrderShipInfo bean) throws ParseException {
    	
        return MessageInfo.ok(orderShipInfoService.doUpdate(bean));
    }
}

