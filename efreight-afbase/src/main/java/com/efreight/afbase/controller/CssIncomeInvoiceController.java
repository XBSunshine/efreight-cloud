package com.efreight.afbase.controller;


import java.util.Map;

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
import com.efreight.afbase.entity.CssIncomeInvoice;
import com.efreight.afbase.entity.CssIncomeInvoiceDetailWriteoff;
import com.efreight.afbase.entity.Statement;
import com.efreight.afbase.service.CssDebitNoteService;
import com.efreight.afbase.service.CssIncomeInvoiceService;
import com.efreight.common.security.util.MessageInfo;

import lombok.AllArgsConstructor;

/**
 * <p>
 * CSS 应收：发票申请表 前端控制器
 * </p>
 *
 * @author cwd
 * @since 2020-12-28
 */
@RestController
@AllArgsConstructor
@RequestMapping("/cssIncomeInvoice")
public class CssIncomeInvoiceController {
	private final CssIncomeInvoiceService cssIncomeInvoiceService;
	/**
	 * 添加
	 *
	 * @param bean 实体
	 * @return success/false
	 * @throws Exception 
	 */
	@PostMapping(value = "/doSave")
	public MessageInfo doSave(@Valid @RequestBody CssIncomeInvoice bean) {
		try{
			return MessageInfo.ok(cssIncomeInvoiceService.doSave(bean));
		}catch (Exception e){
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
	}
	
	@DeleteMapping(value="/{type}/{id}/{rowUuid}")
	public MessageInfo cancelInvoice(@PathVariable("type") String  type,@PathVariable("id") Integer id,@PathVariable("rowUuid") String rowUuid) {
		try {
			if("debitNoteId".equals(type)) {
				cssIncomeInvoiceService.cancelDNInvoice(id,rowUuid);
			}else {
				cssIncomeInvoiceService.cancelSTInvoice(id,rowUuid);
			}
            return MessageInfo.ok();	
        } catch (Exception e) {
        	e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
		
	}
	
	@GetMapping(value="/invoiceList")
	public MessageInfo page(Page page, CssIncomeInvoice bean) {
		 IPage iPage = cssIncomeInvoiceService.getPage(page, bean);
	     return MessageInfo.ok(iPage);
	}
	
	/**
     * 发票列表导出
     *
     * @param bean
     * @return
     */
    @PostMapping("/exportExcelList")
    public void exportExcelList(CssIncomeInvoice bean) {
        try {
        	cssIncomeInvoiceService.exportExcelList(bean);
        } catch (Exception e) {
        	e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
    
	@GetMapping(value="/open/view")
	public MessageInfo openView(CssIncomeInvoice bean) {
		try {
			Map map = cssIncomeInvoiceService.openView(bean);
		    return MessageInfo.ok(map);
		} catch (Exception e) {
			e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
		}
	}
	
	@GetMapping(value="/view/{invoiceId}")
	public MessageInfo invoiceView(@PathVariable("invoiceId") Integer invoiceId) {
		try {
			CssIncomeInvoice c = cssIncomeInvoiceService.invoiceView(invoiceId);
		    return MessageInfo.ok(c);
		} catch (Exception e) {
			e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
		}
	}

}

