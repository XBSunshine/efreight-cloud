package com.efreight.sc.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.sc.entity.ScWarehouse;
import com.efreight.sc.service.ScWarehouseService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/scWarehouse")
@AllArgsConstructor
@Slf4j
public class ScWarehouseController {
	
	private final ScWarehouseService scWarehouseService;
	
	@GetMapping("/page")
    public MessageInfo getPage(Page page, ScWarehouse bean) {
        try {
            IPage result = scWarehouseService.getPage(page, bean);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

	 @PostMapping("/doSave")
	public MessageInfo saveWarehouse(@RequestBody ScWarehouse bean) {
		 try {
			 scWarehouseService.saveWarehouse(bean);
	            return MessageInfo.ok();
	        }catch (Exception e){
	            log.info(e.getMessage());
	            return MessageInfo.failed(e.getMessage());
	        }
	 }

	 @PostMapping("/modify")
     public MessageInfo modifyWarehouse(@RequestBody ScWarehouse bean) {
		 try {
			 scWarehouseService.modifyWarehouse(bean);
	            return MessageInfo.ok();
	        }catch (Exception e){
	            log.info(e.getMessage());
	            return MessageInfo.failed(e.getMessage());
	        }
	 }
	 
	 @PostMapping("/delete")
     public MessageInfo deleteWarehouse(@RequestBody ScWarehouse bean) {
		 try {
			 bean.setWarehouseStatus(0);
			 scWarehouseService.modifyWarehouse(bean);
	            return MessageInfo.ok();
	        }catch (Exception e){
	            log.info(e.getMessage());
	            return MessageInfo.failed(e.getMessage());
	        }
	 }
	 @GetMapping("/list")
     public MessageInfo getList(ScWarehouse bean) {
		 try {
			 bean.setOrgId(SecurityUtils.getUser().getOrgId());
			 List<ScWarehouse> list = scWarehouseService.getList(bean);
	            return MessageInfo.ok(list);
	        }catch (Exception e){
	            log.info(e.getMessage());
	            return MessageInfo.failed(e.getMessage());
	        }
	 }
	 
}
