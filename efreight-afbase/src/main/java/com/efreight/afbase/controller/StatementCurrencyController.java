package com.efreight.afbase.controller;


import java.util.List;

import javax.validation.Valid;

import lombok.AllArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.efreight.afbase.entity.AfIncome;
import com.efreight.afbase.entity.Statement;
import com.efreight.afbase.service.CssDebitNoteCurrencyService;
import com.efreight.afbase.service.StatementCurrencyService;
import com.efreight.common.security.util.MessageInfo;

/**
 * <p>
 * CSS 应收：清单 币种汇总表 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2019-12-26
 */
@RestController
@AllArgsConstructor
@RequestMapping("/listcurrency")
public class StatementCurrencyController {

	private final StatementCurrencyService service;
	
	@GetMapping("/view/{id}")
	public MessageInfo queryBillCurrency(@PathVariable Integer id) {

		 return MessageInfo.ok(service.queryBillCurrency(id));
	}
	

}

