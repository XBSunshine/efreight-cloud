package com.efreight.afbase.controller;


import com.efreight.afbase.entity.IoCost;
import com.efreight.afbase.entity.LcCost;
import com.efreight.afbase.service.IoCostService;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * IO 费用录入 成本 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-09-18
 */
@RestController
@RequestMapping("/io-cost")
@Slf4j
@AllArgsConstructor
public class IoCostController {

    private final IoCostService ioCostService;


    /**
     * 查询成本明细(未完全对账的)
     * @param ioCost
     * @return
     */
    @GetMapping("/list")
    public MessageInfo getCostList(IoCost ioCost) {
        try {
            List<IoCost> result = ioCostService.getCostList(ioCost);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}

