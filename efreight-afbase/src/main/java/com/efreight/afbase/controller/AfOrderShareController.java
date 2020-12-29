package com.efreight.afbase.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.AfOrder;
import com.efreight.afbase.entity.AfOrderShare;
import com.efreight.afbase.service.AfOrderShareService;
import com.efreight.common.security.util.MessageInfo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@RequestMapping("/afOrderShare")
@Slf4j
public class AfOrderShareController {
	private final AfOrderShareService afOrderShareService;
	
	 /**
     * 分页查询信息
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/page/coopList")
    public MessageInfo getListPage(Page page, AfOrderShare bean) {
        return MessageInfo.ok(afOrderShareService.getCoopList(page, bean));
    }
    
    /**
     * 检查当前订单 指定客商资料 是否已经分享
     *
     * @param orderId coopId
     * @return 
     */
    @GetMapping("/afOrderShare/{orderId}/{coopId}/{shareOrgId}")
    public MessageInfo afOrderShareCheck(@PathVariable Integer orderId,@PathVariable Integer coopId,@PathVariable Integer shareOrgId) {
    	boolean flag = afOrderShareService.afOrderShareCheck(orderId,coopId,shareOrgId);
        return MessageInfo.ok(flag);
    }
    /**
     * 根据订单ID 客商ID 查询 PrmCoopShareFields 分享
     *
     * @param orderId coopId
     * @return 
     */
    @GetMapping("/shareFields/{orgId}/{coopId}")
    public MessageInfo queryPrmCoopShareFields(@PathVariable Integer orgId,@PathVariable Integer coopId) {
    	List<String> list = afOrderShareService.queryPrmCoopShareFields(orgId,coopId);
        return MessageInfo.ok(list);
    }
    /**
     * 根据订单ID 客商ID 查询 PrmCoopShareFields 订阅
     *
     * @param orderId coopId
     * @return 
     */
    @GetMapping("/shareFieldsTwo/{orgId}/{coopId}")
    public MessageInfo queryPrmCoopShareFieldsTwo(@PathVariable Integer orgId,@PathVariable Integer coopId) {
    	List<String> list = afOrderShareService.queryPrmCoopShareFieldsTwo(orgId,coopId);
        return MessageInfo.ok(list);
    }
    
    /**
     * 根据订单ID 客商ID 查询 afOrderShare  out类型
     *
     * @param orgId coopId orderId
     * @return 
     */
    @GetMapping("/afOrderShareInfo/{orgId}/{coopId}/{orderId}")
    public MessageInfo afOrderShareInfo(@PathVariable Integer orgId,@PathVariable Integer coopId,@PathVariable Integer orderId) {
    	AfOrderShare aos = afOrderShareService.afOrderShareInfo(orgId,coopId,orderId,"out");
        return MessageInfo.ok(aos);
    }
    
    /**
     * 根据订单ID 客商ID 查询 afOrderShare 扩展 附加类型 
     *
     * @param orgId coopId orderId tyep(out/in)
     * @return 
     */
    @GetMapping("/afOrderShareInfo/{orgId}/{coopId}/{orderId}/{type}")
    public MessageInfo afOrderShareInfoTwo(@PathVariable Integer orgId,@PathVariable Integer coopId,@PathVariable Integer orderId,@PathVariable String type) {
    	AfOrderShare aos = afOrderShareService.afOrderShareInfo(orgId,coopId,orderId,type);
        return MessageInfo.ok(aos);
    }
    
    /**
     * 分享出重
     * @param bean 实体
     * @return success/false
     */
    @PostMapping(value = "/shareInbound")
    public MessageInfo shareInbound(@RequestBody AfOrderShare bean) {
        try {
        	afOrderShareService.shareInbound(bean);
            return MessageInfo.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }
    /**
     * 检查当前订单 是否已经分享
     *
     * @param orderId
     * @return 
     */
    @GetMapping("/afOrderShareCheckOrder/{orderId}")
    public MessageInfo afOrderShareCheckOrder(@PathVariable Integer orderId) {
    	boolean flag = afOrderShareService.afOrderShareCheckOrder(orderId);
        return MessageInfo.ok(flag);
    }
    
    /**
     * 校验订单是否有相关协作记录
     * @param orderId
     * @param shareScope
     * @param orderUuid
     * @return
     */
    @GetMapping("/check/{orderId}/{shareScope}/{orderUuid}")
    public MessageInfo checkShareScope(@PathVariable Integer orderId,@PathVariable String shareScope,@PathVariable String orderUuid) {
    	boolean flag = afOrderShareService.checkShareScope(orderId,shareScope,orderUuid);
        return MessageInfo.ok(flag);
    }
    
    /**
     * 协议传输-电子单证
     * @param bean 实体
     * @return success/false
     */
    @PostMapping(value = "/shareOrderFiles")
    public MessageInfo shareOrderFiles(@RequestBody AfOrderShare bean) {
        try {
        	afOrderShareService.shareOrderFiles(bean);
            return MessageInfo.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }
    /**
     * 协议传输-制单
     * @param bean 实体
     * @return success/false
     */
    @PostMapping(value = "/shareWayBillMake")
    public MessageInfo shareWayBillMake(@RequestBody AfOrderShare bean) {
        try {
        	afOrderShareService.shareWayBillMake(bean);
            return MessageInfo.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
    }
    
}
