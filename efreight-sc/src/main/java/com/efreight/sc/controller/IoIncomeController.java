package com.efreight.sc.controller;


import com.efreight.common.remoteVo.IncomeCostList;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.sc.entity.IoCost;
import com.efreight.sc.entity.IoIncome;
import com.efreight.sc.service.IoIncomeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * IO 费用录入 应收 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-09-17
 */
@RestController
@RequestMapping("/ioIncome")
@AllArgsConstructor
@Slf4j
public class IoIncomeController {

    private final IoIncomeService ioIncomeService;

    /**
     * 获取io收入费用明细
     *
     * @param orderId
     * @return
     */
    @GetMapping("/{orderId}")
    public MessageInfo<List<IoIncome>> list(@PathVariable("orderId") Integer orderId) {
        try {
            List<IoIncome> list = ioIncomeService.getList(orderId);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 修改io收入费用明细
     *
     * @param ioIncome
     * @return
     */
    @PutMapping
    public MessageInfo modify(@RequestBody IoIncome ioIncome) {
        try {
            ioIncomeService.modify(ioIncome);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 新增io收入费用明细
     *
     * @param ioIncome
     * @return
     */
    @PostMapping
    public MessageInfo save(@RequestBody IoIncome ioIncome) {
        try {
            ioIncomeService.insert(ioIncome);
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
            ioIncomeService.delete(incomeId);
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
    public MessageInfo<IoIncome> view(@PathVariable("incomeId") Integer incomeId) {
        try {
            IoIncome ioIncome = ioIncomeService.view(incomeId);
            return MessageInfo.ok(ioIncome);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 保存io费用录入
     *
     * @param incomeCostList
     * @return
     */
    @PostMapping("/saveOrderIncomeAndCost")
    public MessageInfo saveOrderIncomeAndCost(@RequestBody IncomeCostList<IoIncome, IoCost> incomeCostList) {
        try {
            ioIncomeService.saveOrderIncomeAndCost(incomeCostList);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}

