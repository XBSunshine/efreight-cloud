package com.efreight.afbase.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.efreight.afbase.entity.LcCost;
import com.efreight.afbase.service.LcCostService;
import com.efreight.common.security.util.MessageInfo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@RequestMapping("/lc-cost")
@Slf4j
public class LcCostController {
	private final LcCostService service;
	
	   /**
     * 查询成本明细(未完全对账的)
     * @param lcCost
     * @return
     */
    @GetMapping("/list")
    public MessageInfo getCostList(LcCost lcCost) {
        try {
            List<LcCost> result = service.getCostList(lcCost);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}