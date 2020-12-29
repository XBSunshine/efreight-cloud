package com.efreight.sc.controller;


import com.efreight.common.remoteVo.IncomeCostList;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.sc.entity.LcCost;
import com.efreight.sc.entity.LcIncome;
import com.efreight.sc.service.LcIncomeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * LC 费用录入 应收 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-21
 */
@RestController
@RequestMapping("/lcIncome")
@AllArgsConstructor
@Slf4j
public class LcIncomeController {

    private final LcIncomeService lcIncomeService;

    /**
     * 获取lc收入费用明细
     *
     * @param orderId
     * @return
     */
    @GetMapping("/{orderId}")
    public MessageInfo<List<LcIncome>> list(@PathVariable("orderId") Integer orderId) {
        try {
            List<LcIncome> list = lcIncomeService.getList(orderId);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 修改lc收入费用明细
     *
     * @param lcIncome
     * @return
     */
    @PutMapping
    public MessageInfo modify(@RequestBody LcIncome lcIncome) {
        try {
            lcIncomeService.modify(lcIncome);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 新增lc收入费用明细
     *
     * @param lcIncome
     * @return
     */
    @PostMapping
    public MessageInfo save(@RequestBody LcIncome lcIncome) {
        try {
            lcIncomeService.insert(lcIncome);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 删除
     *
     * @param incomeId
     * @return
     */
    @DeleteMapping("/{incomeId}")
    public MessageInfo delete(@PathVariable("incomeId") Integer incomeId) {
        try {
            lcIncomeService.delete(incomeId);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查看详情
     *
     * @param incomeId
     * @return
     */
    @GetMapping("/view/{incomeId}")
    public MessageInfo<LcIncome> view(@PathVariable("incomeId") Integer incomeId) {
        try {
            LcIncome lcIncome = lcIncomeService.view(incomeId);
            return MessageInfo.ok(lcIncome);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 保存lc费用录入
     *
     * @param incomeCostList
     * @return
     */
    @PostMapping("/saveOrderIncomeAndCost")
    public MessageInfo saveOrderIncomeAndCost(@RequestBody IncomeCostList<LcIncome, LcCost> incomeCostList) {
        try {
            lcIncomeService.saveOrderIncomeAndCost(incomeCostList);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}

