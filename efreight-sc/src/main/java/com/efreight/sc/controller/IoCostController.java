package com.efreight.sc.controller;


import com.efreight.common.security.util.MessageInfo;
import com.efreight.sc.entity.IoCost;
import com.efreight.sc.service.IoCostService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * IO 费用录入 成本 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-09-17
 */
@RestController
@RequestMapping("/ioCost")
@AllArgsConstructor
@Slf4j
public class IoCostController {

    private final IoCostService ioCostService;
    /**
     * 获取io成本费用明细
     *
     * @param orderId
     * @return
     */
    @GetMapping("/{orderId}")
    public MessageInfo<List<IoCost>> list(@PathVariable("orderId") Integer orderId) {
        try {
            List<IoCost> list = ioCostService.getList(orderId);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 修改io成本费用明细
     *
     * @param ioCost
     * @return
     */
    @PutMapping
    public MessageInfo modify(@RequestBody IoCost ioCost) {
        try {
            ioCostService.modify(ioCost);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 新增io成本费用明细
     *
     * @param ioCost
     * @return
     */
    @PostMapping
    public MessageInfo save(@RequestBody IoCost ioCost) {
        try {
            ioCostService.insert(ioCost);
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
            ioCostService.delete(costId);
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
    public MessageInfo<IoCost> view(@PathVariable("costId") Integer costId) {
        try {
            IoCost ioCost = ioCostService.view(costId);
            return MessageInfo.ok(ioCost);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}

