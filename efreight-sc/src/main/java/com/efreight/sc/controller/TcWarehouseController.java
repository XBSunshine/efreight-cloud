package com.efreight.sc.controller;


import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.sc.entity.TcWarehouse;
import com.efreight.sc.service.TcWarehouseService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * TC 基础信息 堆场仓库 前端控制器
 * </p>
 *
 * @author caiwd
 * @since 2020-07-14
 */
@RestController
@RequestMapping("/tc-warehouse")
@AllArgsConstructor
@Slf4j
public class TcWarehouseController {
	
	private final TcWarehouseService tcWarehouseService;

	@GetMapping("/page")
	public MessageInfo getPage(Page page, TcWarehouse bean) {
		try {
			IPage result = tcWarehouseService.getPage(page, bean);
			return MessageInfo.ok(result);
		} catch (Exception e) {
			log.info(e.getMessage());
			return MessageInfo.failed(e.getMessage());
		}
	}

    @GetMapping("/te/tcWarehouse")
    public MessageInfo getList(TcWarehouse bean) {
		 try {
			 bean.setOrgId(SecurityUtils.getUser().getOrgId());
			 List<TcWarehouse> list = tcWarehouseService.getList(bean);
	            return MessageInfo.ok(list);
	        }catch (Exception e){
	            log.info(e.getMessage());
	            return MessageInfo.failed(e.getMessage());
	        }
	 }

	@PostMapping("/doSave")
	public MessageInfo saveWarehouse(@RequestBody TcWarehouse bean) {
		try {
			tcWarehouseService.saveWarehouse(bean);
			return MessageInfo.ok();
		}catch (Exception e){
			log.info(e.getMessage());
			return MessageInfo.failed(e.getMessage());
		}
	}

	@PostMapping("/modify")
	public MessageInfo modifyWarehouse(@RequestBody TcWarehouse bean) {
		try {
			tcWarehouseService.modifyWarehouse(bean);
			return MessageInfo.ok();
		}catch (Exception e){
			log.info(e.getMessage());
			return MessageInfo.failed(e.getMessage());
		}
	}

	@PostMapping("/delete")
	public MessageInfo deleteWarehouse(@RequestBody TcWarehouse bean) {
		try {
			bean.setWarehouseStatus(0);
			tcWarehouseService.modifyWarehouse(bean);
			return MessageInfo.ok();
		}catch (Exception e){
			log.info(e.getMessage());
			return MessageInfo.failed(e.getMessage());
		}
	}

}

