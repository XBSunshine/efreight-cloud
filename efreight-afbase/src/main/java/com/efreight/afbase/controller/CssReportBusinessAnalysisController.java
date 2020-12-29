package com.efreight.afbase.controller;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.efreight.afbase.entity.procedure.CssReportBusinessAnalysis;
import com.efreight.afbase.service.CssReportBusinessAnalysisService;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/cssReportBusinessAnalysis")
public class CssReportBusinessAnalysisController {
	
	private final CssReportBusinessAnalysisService service;
	

	@PostMapping(value = "/page")
	public MessageInfo getListPage(CssReportBusinessAnalysis bean) {
		
		List<Map<String,String>> List  = new ArrayList<Map<String,String>>();
		if ("周".equals(bean.getOrderUnit())) {
			List= service.getListPage2(bean);
		}else {
			List= service.getListPage(bean);
		}
		return MessageInfo.ok(List);
	}
	@PostMapping(value = "/getList2")
	public MessageInfo getList2(CssReportBusinessAnalysis bean) {
		
		List<Map<String,String>> List  = new ArrayList<Map<String,String>>();
		if ("周".equals(bean.getOrderUnit())) {
			List= service.getListPage23(bean);
		}else {
			List= service.getListPage22(bean);
		}
		
		return MessageInfo.ok(List);
	}
	@PostMapping(value = "/getList3")
	public MessageInfo getList3(CssReportBusinessAnalysis bean) {
		
		List<Map<String,String>> List  = service.getList3(bean);
		
		return MessageInfo.ok(List);
	}
	@PostMapping(value = "/getList4")
	public MessageInfo getList4(CssReportBusinessAnalysis bean) {
		
		List<Map<String,String>> List  = service.getList4(bean);
		
		return MessageInfo.ok(List);
	}
	@PostMapping(value = "/getList5")
	public MessageInfo getList5(CssReportBusinessAnalysis bean) {
		
		List<Map<String,String>> List  = service.getList5(bean);
		
		return MessageInfo.ok(List);
	}
	
}
