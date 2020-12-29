package com.efreight.afbase.controller;


import java.util.List;

import javax.validation.Valid;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.efreight.afbase.entity.AfIncome;
import com.efreight.afbase.entity.AfIncomeCostTree;
import com.efreight.afbase.entity.IncomeCostList;
import com.efreight.afbase.service.AfIncomeCostService;
import com.efreight.common.security.util.MessageInfo;

/**
 * <p>
 * AF 延伸服务  前端控制器
 * </p>
 *
 * @author qipm
 * @since 2019-10-12
 */
@RestController
@AllArgsConstructor
@RequestMapping("/afIncomeCost")
@Slf4j
public class AfIncomeCostController {
	private final AfIncomeCostService service;

	/**
     * 树型列表查询
     *
     * @param
     * @return
     */
	@GetMapping("/getListTree")
    public MessageInfo getListTree(AfIncome bean) {
        try {
            List<AfIncomeCostTree> list = service.getListTree(bean.getOrderId(),bean.getBusinessScope());
            return MessageInfo.ok(list);
        } catch (Exception e) {
        	log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
	
	@GetMapping("/getIncomeCostList")
    public MessageInfo getIncomeCostList(AfIncome bean) {
        try {
            IncomeCostList list = service.getIncomeCostList(bean);
            return MessageInfo.ok(list);
        } catch (Exception e) {
        	log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
	
	/**
	 * 修改
	 *
	 * @param bean 实体
	 * @return success/false
	 */
	@PostMapping(value = "/doEdit")
	public MessageInfo doEdit(@Valid @RequestBody IncomeCostList bean) {
		try {
			return MessageInfo.ok(service.doEdit(bean));
		} catch (Exception e) {
			log.info(e.getMessage());
			return MessageInfo.failed(e.getMessage());
		}
	}
	
	/**
	 * 应收模板
	 *
	 * @param bean 实体
	 * @return success/false
	 */
	@PostMapping(value = "/addIncomeTemplate")
	public MessageInfo addIncomeTemplate(@Valid @RequestBody AfIncome bean) {
		try {
			return MessageInfo.ok(service.addIncomeTemplate(bean));
		} catch (Exception e) {
			log.info(e.getMessage());
			return MessageInfo.failed(e.getMessage());
		}
	}
	/**
	 * 应付模板
	 *
	 * @param bean 实体
	 * @return success/false
	 */
	@PostMapping(value = "/addCostTemplate")
	public MessageInfo addCostTemplate(@Valid @RequestBody AfIncome bean) {
		try {
			return MessageInfo.ok(service.addCostTemplate(bean));
		} catch (Exception e) {
			log.info(e.getMessage());
			return MessageInfo.failed(e.getMessage());
		}
	}
}

