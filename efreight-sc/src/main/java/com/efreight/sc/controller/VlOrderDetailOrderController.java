package com.efreight.sc.controller;


import com.efreight.sc.service.VlOrderDetailOrderService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * VL 订单管理 派车订单 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-31
 */
@RestController
@RequestMapping("/vlOrderDetailOrder")
@Slf4j
@AllArgsConstructor
public class VlOrderDetailOrderController {
    private final VlOrderDetailOrderService vlOrderDetailOrderService;

}

