package com.efreight.afbase.controller;


import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import lombok.AllArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.efreight.afbase.entity.AfIncome;
import com.efreight.afbase.service.CssDebitNoteCurrencyService;
import com.efreight.common.security.util.MessageInfo;

/**
 * <p>
 * CSS 应收：账单 币种汇总表 前端控制器
 * </p>
 *
 * @author qipm
 * @since 2019-12-24
 */
@RestController
@AllArgsConstructor
@RequestMapping("/billcurrency")
public class CssDebitNoteCurrencyController {
	
	private final CssDebitNoteCurrencyService service;

	
	@PostMapping(value = "/queryBill")
	public MessageInfo queryBill(@Valid @RequestBody List<AfIncome> beans) {

		return MessageInfo.ok(service.queryBill(beans));
	}
	@PostMapping(value = "/queryBill2")
	public MessageInfo queryBill2(@Valid @RequestBody Map<String,Object> map) {
		List<Map<String,Object>> beans=(List<Map<String,Object>>) map.get("selections");
		return MessageInfo.ok(service.queryBill2(String.valueOf(map.get("debitId")),beans));
	}
}

