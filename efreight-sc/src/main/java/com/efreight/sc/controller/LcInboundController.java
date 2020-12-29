package com.efreight.sc.controller;


import com.efreight.common.security.util.MessageInfo;
import com.efreight.sc.entity.LcInbound;
import com.efreight.sc.service.LcInboundService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * LC 陆运订单： 操作出重表 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-28
 */
@RestController
@RequestMapping("/lcInbound")
@AllArgsConstructor
@Slf4j
public class LcInboundController {

    private final LcInboundService lcInboundService;

    /**
     * 通过订单Id查询出重信息
     *
     * @param orderId
     * @return
     */
    @GetMapping("/{orderId}")
    public MessageInfo view(@PathVariable("orderId") Integer orderId) {
        try {
            List<LcInbound> list = lcInboundService.view(orderId);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }


    /**
     * 出重
     *
     * @param lcInbound
     * @return
     */
    @PostMapping
    public MessageInfo save(@RequestBody LcInbound lcInbound) {
        try {
            lcInboundService.insert(lcInbound);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 出重编辑
     *
     * @param lcInbound
     * @return
     */
    @PutMapping
    public MessageInfo modify(@RequestBody LcInbound lcInbound) {
        try {
            lcInboundService.modify(lcInbound);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 删除出重
     *
     * @param inboundId
     * @return
     */
    @DeleteMapping("/{inboundId}/{rowUuid}")
    public MessageInfo save(@PathVariable("inboundId") Integer inboundId,@PathVariable("rowUuid")String rowUuid) {
        try {
            lcInboundService.delete(inboundId,rowUuid);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}

