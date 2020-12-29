package com.efreight.afbase.controller;


import javax.validation.Valid;

import lombok.AllArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.efreight.afbase.entity.AfIncome;
import com.efreight.afbase.entity.CssDebitNote;
import com.efreight.afbase.entity.DebitNote;
import com.efreight.afbase.service.CssDebitNoteService;
import com.efreight.common.security.util.MessageInfo;

/**
 * <p>
 * 清单 前端控制器
 * </p>
 *
 * @author qipm
 * @since 2019-11-06
 */
@RestController
@AllArgsConstructor
@RequestMapping("/cssDebitNote")
public class CssDebitNoteController {
	private final CssDebitNoteService service;
	
	/**
	 * 添加
	 *
	 * @param bean 实体
	 * @return success/false
	 * @throws Exception 
	 */
	@PostMapping(value = "/doSave")
	public MessageInfo doSave(@Valid @RequestBody CssDebitNote bean) {
		try{
			return MessageInfo.ok(service.doSave(bean));
		}catch (Exception e){
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
	}
	@PostMapping(value = "/doEditInvoiceRemark")
	public MessageInfo doEditInvoiceRemark(@Valid @RequestBody DebitNote bean) {
		try{
			return MessageInfo.ok(service.doEditInvoiceRemark(bean));
		}catch (Exception e){
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
	}
	@PostMapping(value = "/doEditInvoiceRemark2")
	public MessageInfo doEditInvoiceRemark2(@Valid @RequestBody DebitNote bean) {
		try{
			return MessageInfo.ok(service.doEditInvoiceRemark2(bean));
		}catch (Exception e){
			e.printStackTrace();
			return MessageInfo.failed(e.getMessage());
		}
	}
	/**
	 * 查询已经存在的账单
	 *
	 * @param bean 实体
	 * @return success/false
	 * @throws Exception 
	 */
	@PostMapping(value = "/queryHavedBill")
	public MessageInfo queryHavedBill(@Valid @RequestBody CssDebitNote bean) {

		return MessageInfo.ok(service.queryHavedBill(bean));
	}
	/**
	 * 查询已经存在的账单明线
	 *
	 * @param bean 实体
	 * @return success/false
	 * @throws Exception 
	 */
	@PostMapping(value = "/queryHavedBillDetail")
	public MessageInfo queryHavedBillDetail(@Valid @RequestBody AfIncome bean) {

		return MessageInfo.ok(service.queryHavedBillDetail(bean));
	}
	/**
	 * 修改
	 *
	 * @param bean 实体
	 * @return success/false
	 * @throws Exception 
	 */
	@PostMapping(value = "/doUpdate")
	public MessageInfo doUpdate(@Valid @RequestBody CssDebitNote bean) {
		try{
			return MessageInfo.ok(service.doUpdate(bean));
		}catch (Exception e){
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
		
	}
}

