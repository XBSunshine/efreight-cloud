package com.efreight.sc.controller;


import com.efreight.common.security.util.MessageInfo;
import com.efreight.sc.entity.LcCost;
import com.efreight.sc.entity.LcIncome;
import com.efreight.sc.service.LcCostService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * LC 费用录入 成本 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-21
 */
@RestController
@RequestMapping("/lcCost")
@AllArgsConstructor
@Slf4j
public class LcCostController {

    private final LcCostService lcCostService;

    /**
     * 获取lc成本费用明细
     *
     * @param orderId
     * @return
     */
    @GetMapping("/{orderId}")
    public MessageInfo<List<LcCost>> list(@PathVariable("orderId") Integer orderId) {
        try {
            List<LcCost> list = lcCostService.getList(orderId);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 修改lc成本费用明细
     *
     * @param lcCost
     * @return
     */
    @PutMapping
    public MessageInfo modify(@RequestBody LcCost lcCost) {
        try {
            lcCostService.modify(lcCost);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 新增lc成本费用明细
     *
     * @param lcCost
     * @return
     */
    @PostMapping
    public MessageInfo save(@RequestBody LcCost lcCost) {
        try {
            lcCostService.insert(lcCost);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 删除成本费用明细
     *
     * @param costId
     * @return
     */
    @DeleteMapping("/{costId}")
    public MessageInfo delete(@PathVariable("costId") Integer costId) {
        try {
            lcCostService.delete(costId);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 查看详情
     *
     * @param costId
     * @return
     */
    @GetMapping("/view/{costId}")
    public MessageInfo<LcCost> view(@PathVariable("costId") Integer costId) {
        try {
            LcCost lcCost = lcCostService.view(costId);
            return MessageInfo.ok(lcCost);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

}

