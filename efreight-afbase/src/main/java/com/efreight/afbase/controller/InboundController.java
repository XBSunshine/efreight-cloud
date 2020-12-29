package com.efreight.afbase.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.Inbound;
import com.efreight.afbase.service.InboundService;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("inbound")
@AllArgsConstructor
@Slf4j
public class InboundController {
    private final InboundService inboundService;

    /**
     * 列表分页查询
     * @param page
     * @param inbound
     * @return
     */
    @GetMapping
    public MessageInfo page(Page page, Inbound inbound) {
        try {
            IPage result = inboundService.getPage(page, inbound);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 删除出重
     * @param number
     * @param flag
     * @return
     */
    @DeleteMapping("/{number}/{flag}")
    @PreAuthorize("@pms.hasPermission('af_button_inbound_delete')")
    public MessageInfo delete(@PathVariable String number,@PathVariable String flag){
        try {
            inboundService.delete(number,flag);
            return MessageInfo.ok();
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 出重时查询列表数据
     * @param number
     * @param flag
     * @return
     */
    @GetMapping("/inbound")
    public MessageInfo inboundView(String number,String flag){
        try {
            List<Inbound> list = inboundService.inboundView(number,flag);
            return MessageInfo.ok(list);
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查询详情
     * @param number
     * @param flag
     * @return
     */
    @GetMapping("/detail")
    public MessageInfo detailView(String number,String flag){
        try {
            List<Inbound> list = inboundService.detailView(number,flag);
            return MessageInfo.ok(list);
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
    /**
     * 订单查询出重详情
     * @param awb_uuid
     * @param order_uuid
     * @return
     */
    @GetMapping("/detail2")
    public MessageInfo detailView2(String awb_uuid,String order_uuid){
//    	try {
    		List<Inbound> list = inboundService.detailView2(awb_uuid,order_uuid);
    		return MessageInfo.ok(list);
//    	}catch (Exception e){
//    		log.info(e.getMessage());
//    		return MessageInfo.failed(e.getMessage());
//    	}
    }

    /**
     * 出重保存
     * @param params
     * @return
     */
    @PostMapping("/saveInbound")
    @PreAuthorize("@pms.hasPermission('af_button_inbound_inbound')")
    public MessageInfo saveInbound(@RequestBody Map<String,List<Inbound>> params){
        try {
            inboundService.saveInbound(params.get("data"));
            return MessageInfo.ok();
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
    
    /**
     * 修改出重
     * @param params
     * @return
     */
    @PostMapping("/modifyInbound")
    public MessageInfo modifyInbound(@RequestBody Inbound inbound){
        try {
            inboundService.modifyInbound(inbound);
            return MessageInfo.ok();
        }catch (Exception e){
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
    /**
     * 查询出重详情
     * @param inboundId
     * @return
     */
    @GetMapping("/detailInbound/{inboundId}")
    public MessageInfo detailInbound(@PathVariable String inboundId){
    	try {
    	     Inbound one = inboundService.getById(Integer.valueOf(inboundId).intValue());
    		return MessageInfo.ok(one);
    	}catch (Exception e){
    		log.info(e.getMessage());
    		return MessageInfo.failed(e.getMessage());
    	}
    }

}
