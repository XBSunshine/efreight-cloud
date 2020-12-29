package com.efreight.afbase.controller;



import javax.validation.Valid;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.CssIncomeWriteoff;
import com.efreight.afbase.entity.Statement;
import com.efreight.afbase.service.CssIncomeWriteoffService;
import com.efreight.common.security.util.MessageInfo;

/**
 * <p>
 * CSS 应收：核销 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-02-03
 */
@RestController
@AllArgsConstructor
@RequestMapping("/cssIncomeWriteoff")
@Slf4j
public class CssIncomeWriteoffController {
	private final CssIncomeWriteoffService service;

	/**
     * 核销列表分页查询
     *
     * @param page
     * @param debitNote
     * @return
     */
    @GetMapping("/page2")
    public MessageInfo page2(Page page, CssIncomeWriteoff bean) {
            IPage result = service.getPage2(page, bean);
            return MessageInfo.ok(result);
    }
    //合计行
    @GetMapping("/getTatol")
	public MessageInfo getTatol(CssIncomeWriteoff bean) {
		return MessageInfo.ok(service.getTatol(bean));
	}
    @GetMapping("/page")
    public MessageInfo page(Page page, CssIncomeWriteoff bean) {
    	IPage result = service.getPage(page, bean);
    	return MessageInfo.ok(result);
    }
	//-------
	@PostMapping(value = "/queryBillCurrency/{debitNoteId}")
	public MessageInfo queryBillCurrency(@PathVariable  Integer debitNoteId) {
		return MessageInfo.ok(service.queryBillCurrency(debitNoteId));
	}

	@PostMapping(value = "/getFinancialAccount/{businessScope}/{customerId}")
	public MessageInfo getFinancialAccount(@PathVariable  String businessScope,@PathVariable  Integer customerId) {
		return MessageInfo.ok(service.getFinancialAccount(businessScope,customerId));
	}

	/**
	 * 核销
	 *
	 * @param bean 核销
	 * @return success/false
	 */
	@PostMapping(value = "/doBillWriteoff")
	public MessageInfo doBillWriteoff(@Valid @RequestBody CssIncomeWriteoff bean) {
		try{
			return MessageInfo.ok(service.doBillWriteoff(bean));
		}catch (Exception e){
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
	}
	//-------
	@PostMapping(value = "/queryListCurrency/{statementId}")
	public MessageInfo queryListCurrency(@PathVariable  Integer statementId) {
		return MessageInfo.ok(service.queryListCurrency(statementId));
	}
	/**
	 * 核销
	 *
	 * @param bean 核销
	 * @return success/false
	 */
	@PostMapping(value = "/doListWriteoff")
	public MessageInfo doListWriteoff(@Valid @RequestBody CssIncomeWriteoff bean) {
		try{
			return MessageInfo.ok(service.doListWriteoff(bean));
		}catch (Exception e){
            e.printStackTrace();
            return MessageInfo.failed(e.getMessage());
        }
	}

	@GetMapping("/getVoucherDate")
	public MessageInfo getVoucherDate(Integer incomeWriteoffId) {
		return MessageInfo.ok(service.getVoucherDate(incomeWriteoffId));
	}

	/**
	 * 删除账单核销
	 *
	 * @param bean 删除账单核销
	 * @return success/false
	 */
	@PostMapping(value = "/doDeleteBillWriteoff")
	public MessageInfo doDeleteBillWriteoff(@Valid @RequestBody CssIncomeWriteoff bean) {
		return MessageInfo.ok(service.doDeleteBillWriteoff(bean));
	}
	/**
	 * 删除清单核销
	 *
	 * @param bean 删除清单核销
	 * @return success/false
	 */
	@PostMapping(value = "/doDeleteListWriteoff")
	public MessageInfo doDeleteListWriteoff(@Valid @RequestBody CssIncomeWriteoff bean) {
		return MessageInfo.ok(service.doDeleteListWriteoff(bean));
	}

	@PostMapping(value = "/queryBillDetail/{incomeWriteoffId}")
	public MessageInfo queryBillDetail(@PathVariable  Integer incomeWriteoffId) {
		return MessageInfo.ok(service.queryBillDetail(incomeWriteoffId));
	}
	@PostMapping(value = "/queryListDetail/{incomeWriteoffId}")
	public MessageInfo queryListDetail(@PathVariable  Integer incomeWriteoffId) {
		return MessageInfo.ok(service.queryListDetail(incomeWriteoffId));
	}
	@GetMapping("/view/{id}")
    public MessageInfo getById(@PathVariable Integer id) {
        return MessageInfo.ok(service.getIncomeWriteoffById(id));
    }
	//----------------
	
	  /**
     * 收入对账核销单列表导出
     *
     * @param bean
     * @return
     */
    @PostMapping("/exportExcelList")
    public void exportExcelList(CssIncomeWriteoff bean) {
        try {
        	service.exportExcelList(bean);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}

