package com.efreight.afbase.controller;


import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.CoopProject;
import com.efreight.afbase.service.CoopProjectService;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;


@RestController
@AllArgsConstructor
@RequestMapping("/coopProject")
public class CoopProjectController {
	
	private final CoopProjectService service;
	/**
	 * 分页查询信息
	 *
	 * @param page 分页对象
	 * @return 分页对象
	 */
	@GetMapping("/page")
	public MessageInfo getListPage(Page page,CoopProject bean) {
		return MessageInfo.ok(service.getListPage(page,bean));
	}
	/**
	 * 添加
	 *
	 * @param dept 实体
	 * @return success/false
	 */
	@PostMapping(value = "/doSave")
	public MessageInfo doSave(@Valid @RequestBody CoopProject bean) {
		return MessageInfo.ok(service.doSave(bean));
	}
	/**
	 * 修改
	 *
	 * @param dept 实体
	 * @return success/false
	 */
	@PostMapping(value = "/doUpdate")
	public MessageInfo doUpdate(@Valid @RequestBody CoopProject bean) {
		return MessageInfo.ok(service.doUpdate(bean));
	}

	@GetMapping("/view/{id}")
    public MessageInfo getById(@PathVariable Integer id) {
        return MessageInfo.ok(service.getById(id));
    }
	/**
	 * 选择币种
	 *
	 * @return list
	 */
	@GetMapping(value = "/selectCurrency")
	public MessageInfo selectCurrency() {
		return MessageInfo.ok(service.selectCurrency());
	}
	/**
	 * 停用
	 *
	 * @param dept 实体
	 * @return success/false
	 */
	@PostMapping(value = "/doStop")
	public MessageInfo doStop(@Valid @RequestBody CoopProject bean) {
		return MessageInfo.ok(service.doStop(bean));
	}
	/**
	 * 锁定
	 *
	 * @param dept 实体
	 * @return success/false
	 */
	@PostMapping(value = "/doLock")
	public MessageInfo doLock(@Valid @RequestBody CoopProject bean) {
		return MessageInfo.ok(service.doLock(bean));
	}
	/**
	 * 解锁
	 *
	 * @param dept 实体
	 * @return success/false
	 */
	@PostMapping(value = "/doUnLock")
	public MessageInfo doUnLock(@Valid @RequestBody CoopProject bean) {
		return MessageInfo.ok(service.doUnLock(bean));
	}
	/**
	 * 延期解锁
	 *
	 * @param dept 实体
	 * @return success/false
	 */
	@PostMapping(value = "/doOpenTime")
	public MessageInfo doOpenTime(@Valid @RequestBody CoopProject bean) {
		return MessageInfo.ok(service.doOpenTime(bean));
	}
}

