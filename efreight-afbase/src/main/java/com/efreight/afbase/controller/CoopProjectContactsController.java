package com.efreight.afbase.controller;


import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.CoopProjectContacts;
import com.efreight.afbase.service.CoopProjectContactsService;
import com.efreight.common.security.util.MessageInfo;

import lombok.AllArgsConstructor;


@RestController
@AllArgsConstructor
@RequestMapping("/coopProjectContacts")
public class CoopProjectContactsController {
	
	private final CoopProjectContactsService service;
	/**
	 * 分页查询信息
	 *
	 * @param page 分页对象
	 * @return 分页对象
	 */
	@GetMapping("/page")
	public MessageInfo getListPage(Page page,CoopProjectContacts bean) {
		return MessageInfo.ok(service.getListPage(page,bean));
	}
	/**
	 * 添加
	 *
	 * @param bean 实体
	 * @return success/false
	 * @throws Exception 
	 */
	@PostMapping(value = "/doSave")
	public MessageInfo doSave(@Valid @RequestBody CoopProjectContacts bean) {
		List<Map<String, Object>> list =service.selectAll(bean);
		if (list.size()>=5) {
			return MessageInfo.failed("最多只能维护5个联系人");
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
	public MessageInfo doUpdate(@Valid @RequestBody CoopProjectContacts bean) {
		return MessageInfo.ok(service.doUpdate(bean));
	}
}

