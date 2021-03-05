package com.efreight.afbase.controller;


import javax.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.efreight.afbase.entity.CssIncomeInvoiceDetail;
import com.efreight.afbase.service.CssIncomeInvoiceDetailService;
import com.efreight.common.security.util.MessageInfo;

import lombok.AllArgsConstructor;

/**
 * <p>
 * CSS 应收：发票明细表 前端控制器
 * </p>
 *
 * @author cwd
 * @since 2020-12-28
 */
@RestController
@AllArgsConstructor
@RequestMapping("/cssIncomeInvoiceDetail")
public class CssIncomeInvoiceDetailController {
	private final CssIncomeInvoiceDetailService cssIncomeInvoiceDetailService;
	/**
	 * 新增发票
	 * @param bean
	 * @return
	 */
	@PostMapping(value = "/doSave")
	public MessageInfo doSave(@Valid @RequestBody CssIncomeInvoiceDetail bean) {
		try{
			return MessageInfo.ok(cssIncomeInvoiceDetailService.doSave(bean));
		}catch (Exception e){
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
	}

	 /**
     * 删除发票
     *
     * @param invoiceDetailId rowUuid
     * @return
     */
    @DeleteMapping(value ="/{invoiceDetailId}/{rowUuid}")
    public MessageInfo deleteInvoiceDetail(@PathVariable("invoiceDetailId") Integer invoiceDetailId,@PathVariable("rowUuid") String rowUuid) {
        try {
        	cssIncomeInvoiceDetailService.deleteInvoiceDetail(invoiceDetailId,rowUuid);
            return MessageInfo.ok();
        } catch (Exception e) {
            return MessageInfo.failed(e.getMessage());
        }

    }
    /**
     * 发票详情
     *
     * @param invoiceDetailId
     * @return
     */
    @GetMapping(value ="/view/{invoiceDetailId}")
    public MessageInfo invoiceDetailInfo(@PathVariable("invoiceDetailId") Integer invoiceDetailId) {
        try {
        	CssIncomeInvoiceDetail detail = cssIncomeInvoiceDetailService.invoiceDetailInfo(invoiceDetailId);
            return MessageInfo.ok(detail);
        } catch (Exception e) {
            return MessageInfo.failed(e.getMessage());
        }

    }
    
}

