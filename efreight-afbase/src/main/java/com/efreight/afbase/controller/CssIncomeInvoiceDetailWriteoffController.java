package com.efreight.afbase.controller;


import javax.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.CssIncomeInvoiceDetailWriteoff;
import com.efreight.afbase.service.CssIncomeInvoiceDetailWriteoffService;
import com.efreight.common.security.util.MessageInfo;

import lombok.AllArgsConstructor;

/**
 * <p>
 * CSS 应收：发票明细 核销表 前端控制器
 * </p>
 *
 * @author cwd
 * @since 2020-12-28
 */
@RestController
@AllArgsConstructor
@RequestMapping("/cssIncomeInvoiceDetailWriteoff")
public class CssIncomeInvoiceDetailWriteoffController {
	
	private final CssIncomeInvoiceDetailWriteoffService service;
	
	/**
	 * 核销
	 * @param bean
	 * @return
	 */
	@PostMapping(value = "/doSave")
	public MessageInfo doSave(@Valid @RequestBody CssIncomeInvoiceDetailWriteoff bean) {
		try{
			return MessageInfo.ok(service.doSave(bean));
		}catch (Exception e){
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
	}
	
	@GetMapping(value="/invoiceWriteoffList")
	public MessageInfo page(Page page, CssIncomeInvoiceDetailWriteoff bean) {
		 IPage iPage = service.getPage(page, bean);
	     return MessageInfo.ok(iPage);
	}

	@GetMapping(value="/view/{invoiceDetailWriteoffId}")
	public MessageInfo viewInfo(@PathVariable("invoiceDetailWriteoffId") Integer invoiceDetailWriteoffId) {
		CssIncomeInvoiceDetailWriteoff info = service.viewInfo(invoiceDetailWriteoffId);
	    return MessageInfo.ok(info);
	}
	 /**
     * 核销单列表导出
     *
     * @param bean
     * @return
     */
    @PostMapping("/exportExcelList")
    public void exportExcelList(CssIncomeInvoiceDetailWriteoff bean) {
        try {
        	service.exportExcelList(bean);
        } catch (Exception e) {
        	e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @DeleteMapping(value="/delete/{invoiceDetailWriteoffId}")
	public MessageInfo deleteInfo(@PathVariable("invoiceDetailWriteoffId") Integer invoiceDetailWriteoffId) {
    	try {
    		service.deleteInfo(invoiceDetailWriteoffId);
    		return MessageInfo.ok();
        } catch (Exception e) {
        	e.printStackTrace();
        	return MessageInfo.failed(e.getMessage());
        }
	}
    
    /**
	 * 自动核销核销
	 * @param bean
	 * @return
	 */
	@PostMapping(value = "/invoiceAuto")
	public MessageInfo invoiceAuto(@Valid @RequestBody CssIncomeInvoiceDetailWriteoff bean) {
		try{
			return MessageInfo.ok(service.invoiceAuto(bean));
		}catch (Exception e){
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
	}
}

