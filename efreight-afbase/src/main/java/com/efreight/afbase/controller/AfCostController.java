package com.efreight.afbase.controller;


import javax.validation.Valid;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.*;

import lombok.AllArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.efreight.afbase.service.AfCostService;
import com.efreight.common.security.util.MessageInfo;

import java.util.List;

/**
 * <p>
 * AF 延伸服务 成本 前端控制器
 * </p>
 *
 * @author qipm
 * @since 2019-10-12
 */
@RestController
@AllArgsConstructor
@RequestMapping("/cost")
@Slf4j
public class AfCostController {

    private final AfCostService service;

    /**
     * 添加
     *
     * @param bean 实体
     * @return success/false
     * @throws Exception
     */
    @PostMapping(value = "/doSave")
    public MessageInfo doSave(@Valid @RequestBody AfCost bean) {

        return MessageInfo.ok(service.doSave(bean));
    }

    @GetMapping("/view/{id}")
    public MessageInfo getById(@PathVariable Integer id) {
        return MessageInfo.ok(service.getById(id));
    }

    /**
     * 修改
     *
     * @param bean 实体
     * @return success/false
     */
    @PostMapping(value = "/doUpdate")
    public MessageInfo doUpdate(@Valid @RequestBody AfCost bean) {
        return MessageInfo.ok(service.doUpdate(bean));
    }

    /**
     * 删除
     *
     * @param bean 实体
     * @return success/false
     */
    @PostMapping(value = "/doDelete")
    public MessageInfo doDelete(@Valid @RequestBody AfCost bean) {
        return MessageInfo.ok(service.doDelete(bean));
    }


    /**
     * 查询成本明细(未完全对账的)
     *
     * @param afCost
     * @return
     */
    @GetMapping
    public MessageInfo getCostList(AfCost afCost) {
        try {
            List<AfCost> result = service.getCostList(afCost);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * report-成本查询
     *
     * @param afCost
     * @return
     */
    @GetMapping("/page")
    public MessageInfo getCostListForAF(Page page, AfCost afCost) {
        try {
            IPage result = null;
            if (afCost.getBusinessScope().startsWith("A")) {
                result = service.getPageForAF(page, afCost);
            } else if (afCost.getBusinessScope().startsWith("S")) {
                ScCost scCost = new ScCost();
                BeanUtils.copyProperties(afCost, scCost);
                result = service.getPageForSC(page, scCost);
            } else if (afCost.getBusinessScope().startsWith("T")) {
                TcCost tcCost = new TcCost();
                BeanUtils.copyProperties(afCost, tcCost);
                result = service.getPageForTC(page, tcCost);
            } else if (afCost.getBusinessScope().startsWith("L")) {
                LcCost lcCost = new LcCost();
                BeanUtils.copyProperties(afCost, lcCost);
                result = service.getPageForLC(page, lcCost);
            }else if (afCost.getBusinessScope().startsWith("I")) {
                IoCost ioCost = new IoCost();
                BeanUtils.copyProperties(afCost, ioCost);
                result = service.getPageForIO(page, ioCost);
            }
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }


    /**
     * 导出Excel
     *
     * @param
     * @return
     */
    @PostMapping("/exportExcel")
    public void exportExcelForAF(AfCost afCost) {
        try {
            if (afCost.getBusinessScope().startsWith("A")) {
                service.exportExcelForAF(afCost);
            } else if (afCost.getBusinessScope().startsWith("S")) {
                ScCost scCost = new ScCost();
                BeanUtils.copyProperties(afCost, scCost);
                service.exportExcelForSC(scCost);
            } else if (afCost.getBusinessScope().startsWith("T")) {
                TcCost tcCost = new TcCost();
                BeanUtils.copyProperties(afCost, tcCost);
                service.exportExcelForTC(tcCost);
            } else if (afCost.getBusinessScope().startsWith("L")) {
                LcCost lcCost = new LcCost();
                BeanUtils.copyProperties(afCost, lcCost);
                service.exportExcelForLC(lcCost);
            }else if (afCost.getBusinessScope().startsWith("I")) {
                IoCost ioCost = new IoCost();
                BeanUtils.copyProperties(afCost, ioCost);
                service.exportExcelForIO(ioCost);
            }
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

}

