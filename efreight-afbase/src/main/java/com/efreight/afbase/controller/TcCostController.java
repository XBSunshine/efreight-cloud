package com.efreight.afbase.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.efreight.afbase.entity.TcCost;
import com.efreight.afbase.service.TcCostService;
import com.efreight.common.security.util.MessageInfo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@RequestMapping("/tc-cost")
@Slf4j
public class TcCostController {
	private final TcCostService service;
	
	   /**
     * 查询成本明细(未完全对账的)
     * @param tcCost
     * @return
     */
    @GetMapping("/list")
    public MessageInfo getCostList(TcCost tcCost) {
        try {
            List<TcCost> result = service.etCostList(tcCost);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
}
