package com.efreight.afbase.controller;


import java.util.List;

import javax.validation.Valid;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.efreight.afbase.entity.AfCost;
import com.efreight.afbase.entity.AfIncome;
import com.efreight.afbase.entity.ScIncome;
import com.efreight.afbase.entity.TcIncome;
import com.efreight.afbase.service.AfIncomeService;
import com.efreight.afbase.service.ScIncomeService;
import com.efreight.afbase.service.TcIncomeService;
import com.efreight.common.security.util.MessageInfo;

/**
 * <p>
 * AF 延伸服务 应收 前端控制器
 * </p>
 *
 * @author qipm
 * @since 2019-10-12
 */
@RestController
@AllArgsConstructor
@RequestMapping("/income")
@Slf4j
public class AfIncomeController {
    private final AfIncomeService service;
    private final ScIncomeService scIncomeService;
    private final TcIncomeService tcIncomeService;

    /**
     * 添加
     *
     * @param dept 实体
     * @return success/false
     * @throws Exception
     */
    @PostMapping(value = "/doSave")
    public MessageInfo doSave(@Valid @RequestBody AfIncome bean) {

        return MessageInfo.ok(service.doSave(bean));
    }

    @GetMapping("/view/{id}/{businessScope}")
    public MessageInfo getById(@PathVariable Integer id, @PathVariable String businessScope) {
        AfIncome bean = new AfIncome();
        if ("AE".equals(businessScope) || "AI".equals(businessScope)) {
            //AfIncome bean=service.getById(id);
            bean = service.getById(id);
            List<AfCost> costs = service.queryByIncomeId(id);
            bean.setCosts(costs);
        } else if ("SE".equals(businessScope) || "SI".equals(businessScope)) {
            ScIncome bean2 = scIncomeService.getById(id);
            BeanUtils.copyProperties(bean2, bean);
            List<AfCost> costs = service.queryByIncomeIdSE(id);
            bean.setCosts(costs);
        }else if(businessScope.startsWith("T")) {
        	TcIncome bean2 = tcIncomeService.getById(id);
            BeanUtils.copyProperties(bean2, bean);
            List<AfCost> costs = service.queryByIncomeIdTC(id);
            bean.setCosts(costs);
        }

        return MessageInfo.ok(bean);
    }

    /**
     * 修改
     *
     * @param dept 实体
     * @return success/false
     */
    @PostMapping(value = "/doUpdate")
    public MessageInfo doUpdate(@Valid @RequestBody AfIncome bean) {
        return MessageInfo.ok(service.doUpdate(bean));
    }

    /**
     * 删除
     *
     * @param dept 实体
     * @return success/false
     */
    @PostMapping(value = "/doDelete")
    public MessageInfo doDelete(@Valid @RequestBody AfIncome bean) {
        return MessageInfo.ok(service.doDelete(bean));
    }

    /**
     * 未出账单查询-财务结算管理使用
     *
     * @param page
     * @param income
     * @return
     */
    @GetMapping
    public MessageInfo page(Page page, AfIncome income) {
        try {
            IPage result = service.getPage(page, income);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 导出未出账单-财务结算管理使用
     *
     * @param income
     * @return
     */
    @PostMapping("/exportExcel")
    public void exportExcel(AfIncome income) {
        try {
            service.exportExcel(income);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }
}

