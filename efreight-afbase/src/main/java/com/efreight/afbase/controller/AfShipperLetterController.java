package com.efreight.afbase.controller;


import javax.validation.Valid;

import com.efreight.afbase.entity.AfOrder;
import lombok.AllArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.efreight.afbase.entity.AfShipperLetter;
import com.efreight.afbase.service.AfShipperLetterService;
import com.efreight.common.security.util.MessageInfo;

import java.util.List;


/**
 * <p>
 * AF 订单管理 出口订单 托书信息 前端控制器
 * </p>
 *
 * @author qipm
 * @since 2019-10-10
 */
@RestController
@AllArgsConstructor
@RequestMapping("/afShipperLetter")
public class AfShipperLetterController {
	private final AfShipperLetterService service;
	/**
	 * 分页查询信息
	 *
	 * @param page 分页对象
	 * @return 分页对象
	 */
	@GetMapping("/page")
	public MessageInfo getListPage(AfShipperLetter bean) {
		return MessageInfo.ok(service.getListPage(bean));
	}
	/**
	 * 添加
	 *
	 * @param dept 实体
	 * @return success/false
	 * @throws Exception 
	 */
	@PreAuthorize("@pms.hasPermission('af-order-letter')")
	@PostMapping(value = "/doSave")
	public MessageInfo doSave(@Valid @RequestBody AfShipperLetter bean) {

		return MessageInfo.ok(service.doSave(bean));
	}
	/**
	 * 修改
	 *
	 * @param dept 实体
	 * @return success/false
	 */
	@PreAuthorize("@pms.hasPermission('af-order-letter')")
	@PostMapping(value = "/doUpdate")
	public MessageInfo doUpdate(@Valid @RequestBody AfShipperLetter bean) {
		return MessageInfo.ok(service.doUpdate(bean));
	}
	/**
	 * 删除
	 *
	 * @param dept 实体
	 * @return success/false
	 */
	@PreAuthorize("@pms.hasPermission('af-order-letter')")
	@PostMapping(value = "/doDelete")
	public MessageInfo doDelete(@Valid @RequestBody AfShipperLetter bean) {
		return MessageInfo.ok(service.doDelete(bean));
	}
}

