package com.efreight.afbase.controller;


import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.efreight.afbase.entity.ScCost;
import com.efreight.afbase.service.ScCostService;
import com.efreight.common.security.util.MessageInfo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * CS 延伸服务 成本 前端控制器
 * </p>
 *
 * @author qipm
 * @since 2020-03-06
 */
@RestController
@AllArgsConstructor
@RequestMapping("/afbase/sc-cost")
@Slf4j
public class ScCostController {
	private final ScCostService service;
	   /**
     * 查询成本明细(未完全对账的)
     * @param scCost
     * @return
     */
    @GetMapping
    public MessageInfo getCostList(ScCost scCost) {
        try {
            List<ScCost> result = service.getCostList(scCost);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

}

